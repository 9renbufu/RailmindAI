package com.railmind.train.service;

import com.railmind.train.dto.ScheduleCreateRequest;
import com.railmind.train.vo.ScheduleVO;

public interface ScheduleService {

    /**
     * 查询运行计划
     */
    ScheduleVO getSchedule(String trainNo, java.time.LocalDate travelDate);

    /**
     * 创建运行计划
     */
    ScheduleVO createSchedule(ScheduleCreateRequest request);
}
