package com.railmind.ticket.controller;

import com.railmind.common.model.Result;
import com.railmind.ticket.dto.WaitlistCancelRequest;
import com.railmind.ticket.dto.WaitlistJoinRequest;
import com.railmind.ticket.service.WaitlistService;
import com.railmind.ticket.vo.WaitlistPositionVO;
import com.railmind.ticket.vo.WaitlistVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ticket/waitlist")
@RequiredArgsConstructor
@Tag(name = "候补购票", description = "候补购票管理接口")
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping("/join")
    @Operation(summary = "加入候补队列", description = "当余票不足时加入候补购票队列")
    public Result<WaitlistVO> joinWaitlist(@Valid @RequestBody WaitlistJoinRequest request) {
        log.info("加入候补队列请求: userId={}, trainId={}, date={}", request.getUserId(), request.getTrainId(), request.getTravelDate());
        WaitlistVO result = waitlistService.joinWaitlist(request);
        return Result.ok(result);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消候补", description = "取消候补购票")
    public Result<Void> cancelWaitlist(@Valid @RequestBody WaitlistCancelRequest request) {
        log.info("取消候补请求: waitlistId={}, userId={}", request.getWaitlistId(), request.getUserId());
        waitlistService.cancelWaitlist(request);
        return Result.ok();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户候补列表", description = "查询用户所有候补购票记录")
    public Result<List<WaitlistVO>> getUserWaitlist(
            @PathVariable @Parameter(description = "用户ID") Long userId) {
        List<WaitlistVO> waitlist = waitlistService.getUserWaitlist(userId);
        return Result.ok(waitlist);
    }

    @GetMapping("/position/{waitlistId}")
    @Operation(summary = "获取候补排队位置", description = "查询候补的当前排队位置和预估等待时间")
    public Result<WaitlistPositionVO> getWaitlistPosition(
            @PathVariable @Parameter(description = "候补ID") Long waitlistId) {
        WaitlistPositionVO position = waitlistService.getWaitlistPosition(waitlistId);
        return Result.ok(position);
    }

    @PostMapping("/cancel-expired")
    @Operation(summary = "取消过期候补", description = "取消所有过期的候补记录（定时任务调用）")
    public Result<Integer> cancelExpiredWaitlist() {
        log.info("手动触发取消过期候补");
        int count = waitlistService.cancelExpiredWaitlist();
        return Result.ok(count);
    }
}
