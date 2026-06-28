package com.railmind.train.service.impl;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.train.domain.model.Train;
import com.railmind.train.domain.model.TrainSchedule;
import com.railmind.train.dto.ScheduleCreateRequest;
import com.railmind.train.mapper.TrainMapper;
import com.railmind.train.mapper.TrainScheduleMapper;
import com.railmind.train.service.ScheduleService;
import com.railmind.train.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final TrainMapper trainMapper;
    private final TrainScheduleMapper trainScheduleMapper;

    @Override
    public ScheduleVO getSchedule(String trainNo, LocalDate travelDate) {
        log.info("查询运行计划: trainNo={}, date={}", trainNo, travelDate);

        Train train = trainMapper.selectByTrainNo(trainNo);
        if (train == null) {
            throw new BizException(ErrorCode.TRAIN_NOT_FOUND);
        }

        TrainSchedule schedule = trainScheduleMapper.selectByTrainIdAndDate(train.getId(), travelDate);
        if (schedule == null) {
            throw new BizException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        return toScheduleVO(schedule, trainNo);
    }

    @Override
    @Transactional
    public ScheduleVO createSchedule(ScheduleCreateRequest request) {
        log.info("创建运行计划: {}", request);

        Train train = trainMapper.selectByTrainNo(request.getTrainNo());
        if (train == null) {
            throw new BizException(ErrorCode.TRAIN_NOT_FOUND);
        }

        // 检查是否已存在
        TrainSchedule existing = trainScheduleMapper.selectByTrainIdAndDate(train.getId(), request.getTravelDate());
        if (existing != null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该日期的运行计划已存在");
        }

        TrainSchedule schedule = TrainSchedule.builder()
                .trainId(train.getId())
                .travelDate(request.getTravelDate())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .build();

        trainScheduleMapper.insert(schedule);

        log.info("运行计划创建成功: id={}", schedule.getId());

        return toScheduleVO(schedule, request.getTrainNo());
    }

    private ScheduleVO toScheduleVO(TrainSchedule schedule, String trainNo) {
        String statusName = switch (schedule.getStatus()) {
            case 0 -> "停运";
            case 1 -> "正常";
            case 2 -> "加开";
            default -> "未知";
        };

        return ScheduleVO.builder()
                .id(schedule.getId())
                .trainId(schedule.getTrainId())
                .trainNo(trainNo)
                .travelDate(schedule.getTravelDate())
                .status(schedule.getStatus())
                .statusName(statusName)
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}
