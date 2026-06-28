package com.railmind.ticket.service.impl;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.ticket.domain.model.Waitlist;
import com.railmind.ticket.dto.WaitlistCancelRequest;
import com.railmind.ticket.dto.WaitlistJoinRequest;
import com.railmind.ticket.mapper.WaitlistMapper;
import com.railmind.ticket.service.WaitlistService;
import com.railmind.ticket.vo.WaitlistPositionVO;
import com.railmind.ticket.vo.WaitlistVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistServiceImpl implements WaitlistService {

    private final WaitlistMapper waitlistMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String WAITLIST_KEY_PREFIX = "waitlist:queue:";
    private static final int DEFAULT_PRIORITY = 100;
    private static final int DEFAULT_EXPIRE_HOURS = 24;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WaitlistVO joinWaitlist(WaitlistJoinRequest request) {
        log.info("加入候补队列: userId={}, trainId={}, date={}, from={}, to={}, seatType={}",
                request.getUserId(), request.getTrainId(), request.getTravelDate(),
                request.getFromStation(), request.getToStation(), request.getSeatTypeCode());

        // 1. 检查用户是否已在该车次候补队列中
        List<Waitlist> existingList = waitlistMapper.selectByRoute(
                request.getTrainId(), request.getTravelDate(),
                request.getFromStation(), request.getToStation(), request.getSeatTypeCode());

        boolean alreadyInQueue = existingList.stream()
                .anyMatch(w -> w.getUserId().equals(request.getUserId()));
        if (alreadyInQueue) {
            throw new BizException(ErrorCode.ALREADY_IN_WAITLIST, "您已在候补队列中，请勿重复提交");
        }

        // 2. 计算优先级（基础优先级 + 用户等级加分）
        int priority = calculatePriority(request.getUserId());

        // 3. 创建候补记录
        LocalDateTime now = LocalDateTime.now();
        Waitlist waitlist = Waitlist.builder()
                .userId(request.getUserId())
                .trainId(request.getTrainId())
                .travelDate(request.getTravelDate())
                .fromStation(request.getFromStation())
                .toStation(request.getToStation())
                .seatTypeCode(request.getSeatTypeCode())
                .passengerIds(request.getPassengerIds())
                .priority(priority)
                .status("WAITING")
                .expireAt(now.plusHours(DEFAULT_EXPIRE_HOURS))
                .build();

        waitlistMapper.insert(waitlist);

        // 4. 写入Redis ZSet（score = priority * 1000000000 + timestamp倒序）
        String redisKey = buildWaitlistKey(request.getTrainId(), request.getTravelDate(),
                request.getFromStation(), request.getToStation(), request.getSeatTypeCode());

        double score = priority * 1000000000.0 + (System.currentTimeMillis() / 1000.0);
        redisTemplate.opsForZSet().add(redisKey, String.valueOf(waitlist.getId()), score);

        log.info("候补加入成功: waitlistId={}, priority={}, position={}",
                waitlist.getId(), priority, getQueuePosition(redisKey, waitlist.getId()));

        return convertToVO(waitlist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWaitlist(WaitlistCancelRequest request) {
        log.info("取消候补: waitlistId={}, userId={}", request.getWaitlistId(), request.getUserId());

        Waitlist waitlist = waitlistMapper.selectById(request.getWaitlistId());
        if (waitlist == null) {
            throw new BizException(ErrorCode.WAITLIST_NOT_FOUND, "候补记录不存在");
        }

        if (!waitlist.getUserId().equals(request.getUserId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权取消此候补");
        }

        if (!"WAITING".equals(waitlist.getStatus())) {
            throw new BizException(ErrorCode.WAITLIST_ALREADY_PROCESSED, "候补已处理，无法取消");
        }

        // 更新数据库状态
        waitlistMapper.updateStatus(waitlist.getId(), "CANCELLED");

        // 从Redis ZSet中移除
        String redisKey = buildWaitlistKey(waitlist.getTrainId(), waitlist.getTravelDate(),
                waitlist.getFromStation(), waitlist.getToStation(), waitlist.getSeatTypeCode());
        redisTemplate.opsForZSet().remove(redisKey, String.valueOf(waitlist.getId()));

        log.info("候补取消成功: waitlistId={}", request.getWaitlistId());
    }

    @Override
    public List<WaitlistVO> getUserWaitlist(Long userId) {
        List<Waitlist> waitlistList = waitlistMapper.selectUserWaitlist(userId);
        return waitlistList.stream().map(this::convertToVO).toList();
    }

    @Override
    public WaitlistPositionVO getWaitlistPosition(Long waitlistId) {
        Waitlist waitlist = waitlistMapper.selectById(waitlistId);
        if (waitlist == null) {
            throw new BizException(ErrorCode.WAITLIST_NOT_FOUND, "候补记录不存在");
        }

        if (!"WAITING".equals(waitlist.getStatus())) {
            return WaitlistPositionVO.builder()
                    .waitlistId(waitlistId)
                    .position(0)
                    .aheadCount(0)
                    .estimatedWaitMinutes(0)
                    .status(waitlist.getStatus())
                    .build();
        }

        // 获取排队位置
        String redisKey = buildWaitlistKey(waitlist.getTrainId(), waitlist.getTravelDate(),
                waitlist.getFromStation(), waitlist.getToStation(), waitlist.getSeatTypeCode());

        int position = getQueuePosition(redisKey, waitlistId);
        int aheadCount = Math.max(0, position - 1);

        // 简单估算：每人平均等待30分钟
        int estimatedWaitMinutes = aheadCount * 30;

        return WaitlistPositionVO.builder()
                .waitlistId(waitlistId)
                .position(position)
                .aheadCount(aheadCount)
                .estimatedWaitMinutes(estimatedWaitMinutes)
                .status("WAITING")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WaitlistVO> fulfillWaitlist(Long trainId, LocalDate travelDate,
                                             String fromStation, String toStation,
                                             String seatTypeCode, int count) {
        log.info("处理候补兑现: trainId={}, date={}, from={}, to={}, seatType={}, count={}",
                trainId, travelDate, fromStation, toStation, seatTypeCode, count);

        String redisKey = buildWaitlistKey(trainId, travelDate, fromStation, toStation, seatTypeCode);

        // 从Redis ZSet中获取优先级最高的候补
        Set<ZSetOperations.TypedTuple<String>> topEntries =
                redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, 0, count - 1);

        if (topEntries == null || topEntries.isEmpty()) {
            log.info("候补队列为空");
            return new ArrayList<>();
        }

        List<WaitlistVO> fulfilledList = new ArrayList<>();

        for (ZSetOperations.TypedTuple<String> entry : topEntries) {
            String waitlistIdStr = entry.getValue();
            if (waitlistIdStr == null) continue;

            Long waitlistId = Long.parseLong(waitlistIdStr);
            Waitlist waitlist = waitlistMapper.selectById(waitlistId);

            if (waitlist == null || !"WAITING".equals(waitlist.getStatus())) {
                // 已处理的候补，从队列中移除
                redisTemplate.opsForZSet().remove(redisKey, waitlistIdStr);
                continue;
            }

            // 检查是否过期
            if (waitlist.getExpireAt().isBefore(LocalDateTime.now())) {
                waitlistMapper.updateStatus(waitlistId, "CANCELLED");
                redisTemplate.opsForZSet().remove(redisKey, waitlistIdStr);
                continue;
            }

            // 更新状态为已兑现
            waitlistMapper.updateStatus(waitlistId, "FULFILLED");
            redisTemplate.opsForZSet().remove(redisKey, waitlistIdStr);

            fulfilledList.add(convertToVO(waitlist));
            log.info("候补兑现成功: waitlistId={}, userId={}", waitlistId, waitlist.getUserId());

            if (fulfilledList.size() >= count) break;
        }

        log.info("候补兑现完成: fulfilledCount={}", fulfilledList.size());
        return fulfilledList;
    }

    @Override
    public int cancelExpiredWaitlist() {
        log.info("开始取消过期候补");
        int count = waitlistMapper.cancelExpiredWaitlist();
        log.info("取消过期候补完成: count={}", count);
        return count;
    }

    private int calculatePriority(Long userId) {
        // 简单的优先级计算：基础分 + 用户等级加分
        // 实际应该根据用户VIP等级、历史购票记录等计算
        return DEFAULT_PRIORITY;
    }

    private String buildWaitlistKey(Long trainId, LocalDate travelDate,
                                    String fromStation, String toStation, String seatTypeCode) {
        return WAITLIST_KEY_PREFIX + trainId + ":" + travelDate + ":" +
                fromStation + ":" + toStation + ":" + seatTypeCode;
    }

    private int getQueuePosition(String redisKey, Long waitlistId) {
        Set<String> allMembers = redisTemplate.opsForZSet().reverseRange(redisKey, 0, -1);
        if (allMembers == null) return -1;

        int position = 1;
        for (String member : allMembers) {
            if (member.equals(String.valueOf(waitlistId))) {
                return position;
            }
            position++;
        }
        return -1;
    }

    private WaitlistVO convertToVO(Waitlist waitlist) {
        return WaitlistVO.builder()
                .id(waitlist.getId())
                .userId(waitlist.getUserId())
                .trainId(waitlist.getTrainId())
                .travelDate(waitlist.getTravelDate())
                .fromStation(waitlist.getFromStation())
                .toStation(waitlist.getToStation())
                .seatTypeCode(waitlist.getSeatTypeCode())
                .passengerIds(waitlist.getPassengerIds())
                .priority(waitlist.getPriority())
                .status(waitlist.getStatus())
                .expireAt(waitlist.getExpireAt())
                .createdAt(waitlist.getCreatedAt())
                .build();
    }
}
