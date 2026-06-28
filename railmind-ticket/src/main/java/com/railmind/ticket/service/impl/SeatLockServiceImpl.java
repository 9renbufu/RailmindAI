package com.railmind.ticket.service.impl;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.ticket.domain.model.SeatLock;
import com.railmind.ticket.dto.LockSeatRequest;
import com.railmind.ticket.mapper.SeatLockMapper;
import com.railmind.ticket.service.SeatLockService;
import com.railmind.ticket.vo.SeatLockVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockServiceImpl implements SeatLockService {

    private final SeatLockMapper seatLockMapper;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    private static final String SEAT_LOCK_KEY_PREFIX = "seat:lock:";
    private static final String SEAT_LOCK_DETAIL_KEY_PREFIX = "seat:lock:detail:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeatLockVO lockSeat(LockSeatRequest request) {
        log.info("锁定座位: trainId={}, date={}, seatType={}, seatNo={}, userId={}",
                request.getTrainId(), request.getTravelDate(), request.getSeatTypeCode(),
                request.getSeatNo(), request.getUserId());

        // 1. 构建分布式锁Key
        String lockKey = buildLockKey(request.getTrainId(), request.getTravelDate(),
                request.getSeatTypeCode(), request.getSeatNo());

        // 2. 获取Redisson分布式锁
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁，最多等待3秒，锁自动过期15分钟
            if (!lock.tryLock(3, 15 * 60, TimeUnit.SECONDS)) {
                throw new BizException(ErrorCode.SEAT_LOCKED, "座位已被其他用户锁定");
            }

            // 3. 检查数据库中是否有活跃锁
            SeatLock existingLock = seatLockMapper.selectActiveLock(
                    request.getTrainId(), request.getTravelDate(),
                    request.getSeatTypeCode(), request.getSeatNo());

            if (existingLock != null) {
                // 如果是同一用户的同一订单，允许重入
                if (existingLock.getUserId().equals(request.getUserId()) &&
                    (request.getOrderNo() == null || request.getOrderNo().equals(existingLock.getOrderNo()))) {
                    log.info("座位已被当前用户锁定，返回现有锁: lockId={}", existingLock.getId());
                    return convertToVO(existingLock);
                }
                throw new BizException(ErrorCode.SEAT_LOCKED, "座位已被其他用户锁定");
            }

            // 4. 如果未指定座位号，自动分配一个可用座位
            String seatNo = request.getSeatNo();
            if (seatNo == null || seatNo.isBlank()) {
                seatNo = autoAssignSeat(request.getTrainId(), request.getTravelDate(),
                        request.getSeatTypeCode());
                if (seatNo == null) {
                    throw new BizException(ErrorCode.NO_AVAILABLE_SEAT, "没有可用座位");
                }
            }

            // 5. 创建座位锁记录
            LocalDateTime now = LocalDateTime.now();
            SeatLock seatLock = SeatLock.builder()
                    .trainId(request.getTrainId())
                    .travelDate(request.getTravelDate())
                    .seatTypeCode(request.getSeatTypeCode())
                    .seatNo(seatNo)
                    .orderNo(request.getOrderNo())
                    .userId(request.getUserId())
                    .lockTime(now)
                    .expireTime(now.plusMinutes(request.getLockMinutes()))
                    .status(1)
                    .build();

            seatLockMapper.insert(seatLock);

            // 6. 写入Redis缓存
            String redisKey = buildLockDetailKey(request.getTrainId(), request.getTravelDate(), seatNo);
            redisTemplate.opsForValue().set(redisKey, request.getOrderNo() != null ? request.getOrderNo() : "",
                    request.getLockMinutes(), TimeUnit.MINUTES);

            log.info("座位锁定成功: lockId={}, seatNo={}", seatLock.getId(), seatNo);
            return convertToVO(seatLock);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ErrorCode.SYSTEM_ERROR, "获取锁被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseLock(Long lockId, Long userId) {
        log.info("释放座位锁: lockId={}, userId={}", lockId, userId);

        SeatLock seatLock = seatLockMapper.selectById(lockId);
        if (seatLock == null) {
            throw new BizException(ErrorCode.LOCK_NOT_FOUND, "座位锁不存在");
        }

        if (!seatLock.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权释放此座位锁");
        }

        if (seatLock.getStatus() == 0) {
            log.warn("座位锁已释放: lockId={}", lockId);
            return;
        }

        // 释放数据库锁
        seatLockMapper.releaseLock(lockId);

        // 删除Redis缓存
        String redisKey = buildLockDetailKey(seatLock.getTrainId(), seatLock.getTravelDate(), seatLock.getSeatNo());
        redisTemplate.delete(redisKey);

        log.info("座位锁释放成功: lockId={}", lockId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseLockByOrderNo(String orderNo) {
        log.info("根据订单号释放座位锁: orderNo={}", orderNo);

        List<SeatLock> locks = seatLockMapper.selectByOrderNo(orderNo);
        if (locks.isEmpty()) {
            log.warn("未找到关联的座位锁: orderNo={}", orderNo);
            return;
        }

        for (SeatLock lock : locks) {
            seatLockMapper.releaseLock(lock.getId());

            String redisKey = buildLockDetailKey(lock.getTrainId(), lock.getTravelDate(), lock.getSeatNo());
            redisTemplate.delete(redisKey);
        }

        log.info("订单关联座位锁释放完成: orderNo={}, count={}", orderNo, locks.size());
    }

    @Override
    public List<String> getLockedSeats(Long trainId, LocalDate travelDate, String seatTypeCode) {
        return seatLockMapper.selectLockedSeats(trainId, travelDate, seatTypeCode);
    }

    @Override
    public List<SeatLockVO> getUserActiveLocks(Long userId) {
        List<SeatLock> locks = seatLockMapper.selectUserActiveLocks(userId);
        return locks.stream().map(this::convertToVO).toList();
    }

    @Override
    public SeatLockVO getLockDetail(Long lockId) {
        SeatLock seatLock = seatLockMapper.selectById(lockId);
        if (seatLock == null) {
            throw new BizException(ErrorCode.LOCK_NOT_FOUND, "座位锁不存在");
        }
        return convertToVO(seatLock);
    }

    @Override
    public int cleanExpiredLocks() {
        log.info("开始清理过期座位锁");
        int count = seatLockMapper.releaseExpiredLocks();
        log.info("清理过期座位锁完成: count={}", count);
        return count;
    }

    private String autoAssignSeat(Long trainId, LocalDate travelDate, String seatTypeCode) {
        // 获取已锁定的座位列表
        List<String> lockedSeats = seatLockMapper.selectLockedSeats(trainId, travelDate, seatTypeCode);

        // 简单的座位分配逻辑：从1号车厢1A开始尝试
        // 实际应该根据座位类型和车厢配置来分配
        for (int car = 1; car <= 16; car++) {
            for (char seat = 'A'; seat <= 'F'; seat++) {
                String seatNo = String.format("%02d车%c", car, seat);
                if (!lockedSeats.contains(seatNo)) {
                    return seatNo;
                }
            }
        }
        return null;
    }

    private String buildLockKey(Long trainId, LocalDate travelDate, String seatTypeCode, String seatNo) {
        return SEAT_LOCK_KEY_PREFIX + trainId + ":" + travelDate + ":" + seatTypeCode + ":" + seatNo;
    }

    private String buildLockDetailKey(Long trainId, LocalDate travelDate, String seatNo) {
        return SEAT_LOCK_DETAIL_KEY_PREFIX + trainId + ":" + travelDate + ":" + seatNo;
    }

    private SeatLockVO convertToVO(SeatLock seatLock) {
        long remainingSeconds = 0;
        if (seatLock.getStatus() == 1 && seatLock.getExpireTime() != null) {
            remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), seatLock.getExpireTime());
            if (remainingSeconds < 0) remainingSeconds = 0;
        }

        return SeatLockVO.builder()
                .id(seatLock.getId())
                .trainId(seatLock.getTrainId())
                .travelDate(seatLock.getTravelDate())
                .seatTypeCode(seatLock.getSeatTypeCode())
                .seatNo(seatLock.getSeatNo())
                .orderNo(seatLock.getOrderNo())
                .userId(seatLock.getUserId())
                .lockTime(seatLock.getLockTime())
                .expireTime(seatLock.getExpireTime())
                .status(seatLock.getStatus())
                .remainingSeconds(remainingSeconds)
                .build();
    }
}
