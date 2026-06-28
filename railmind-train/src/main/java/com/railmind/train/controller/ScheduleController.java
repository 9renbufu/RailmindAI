package com.railmind.train.controller;

import com.railmind.common.model.Result;
import com.railmind.train.dto.ScheduleCreateRequest;
import com.railmind.train.dto.ScheduleQueryRequest;
import com.railmind.train.service.ScheduleService;
import com.railmind.train.vo.ScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@Tag(name = "运行图管理", description = "车次运行计划接口")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @Operation(summary = "查询运行计划", description = "查询指定车次和日期的运行计划")
    public Result<ScheduleVO> getSchedule(@Valid ScheduleQueryRequest request) {
        ScheduleVO schedule = scheduleService.getSchedule(request.getTrainNo(), request.getTravelDate());
        return Result.ok(schedule);
    }

    @PostMapping("/create")
    @Operation(summary = "创建运行计划", description = "按模板生成某日运行计划")
    public Result<ScheduleVO> createSchedule(@Valid @RequestBody ScheduleCreateRequest request) {
        ScheduleVO schedule = scheduleService.createSchedule(request);
        return Result.ok(schedule);
    }
}
