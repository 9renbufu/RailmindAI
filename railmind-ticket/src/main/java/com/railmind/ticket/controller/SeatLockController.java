package com.railmind.ticket.controller;

import com.railmind.common.model.Result;
import com.railmind.ticket.dto.LockSeatRequest;
import com.railmind.ticket.service.SeatLockService;
import com.railmind.ticket.vo.SeatLockVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ticket/seat-lock")
@RequiredArgsConstructor
@Tag(name = "座位锁定", description = "座位锁定与释放接口")
public class SeatLockController {

    private final SeatLockService seatLockService;

    @PostMapping("/lock")
    @Operation(summary = "锁定座位", description = "使用分布式锁锁定指定座位")
    public Result<SeatLockVO> lockSeat(@Valid @RequestBody LockSeatRequest request) {
        log.info("锁定座位请求: trainId={}, seatNo={}, userId={}", request.getTrainId(), request.getSeatNo(), request.getUserId());
        SeatLockVO result = seatLockService.lockSeat(request);
        return Result.ok(result);
    }

    @PostMapping("/release/{lockId}")
    @Operation(summary = "释放座位锁", description = "释放指定的座位锁")
    public Result<Void> releaseLock(
            @PathVariable @Parameter(description = "锁定ID") Long lockId,
            @RequestParam @Parameter(description = "用户ID") Long userId) {
        log.info("释放座位锁请求: lockId={}, userId={}", lockId, userId);
        seatLockService.releaseLock(lockId, userId);
        return Result.ok();
    }

    @PostMapping("/release-by-order/{orderNo}")
    @Operation(summary = "根据订单号释放座位锁", description = "释放关联指定订单的所有座位锁")
    public Result<Void> releaseLockByOrderNo(
            @PathVariable @Parameter(description = "订单号") String orderNo) {
        log.info("根据订单号释放座位锁: orderNo={}", orderNo);
        seatLockService.releaseLockByOrderNo(orderNo);
        return Result.ok();
    }

    @GetMapping("/locked-seats")
    @Operation(summary = "获取已锁定座位列表", description = "查询指定车次/日期/座位类型的已锁定座位")
    public Result<List<String>> getLockedSeats(
            @RequestParam @Parameter(description = "车次ID") Long trainId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Parameter(description = "乘车日期") LocalDate travelDate,
            @RequestParam @Parameter(description = "座位类型编码") String seatTypeCode) {
        List<String> lockedSeats = seatLockService.getLockedSeats(trainId, travelDate, seatTypeCode);
        return Result.ok(lockedSeats);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户活跃座位锁", description = "查询用户当前所有活跃的座位锁")
    public Result<List<SeatLockVO>> getUserActiveLocks(
            @PathVariable @Parameter(description = "用户ID") Long userId) {
        List<SeatLockVO> locks = seatLockService.getUserActiveLocks(userId);
        return Result.ok(locks);
    }

    @GetMapping("/{lockId}")
    @Operation(summary = "获取座位锁详情", description = "根据ID获取座位锁详细信息")
    public Result<SeatLockVO> getLockDetail(
            @PathVariable @Parameter(description = "锁定ID") Long lockId) {
        SeatLockVO lock = seatLockService.getLockDetail(lockId);
        return Result.ok(lock);
    }

    @PostMapping("/clean-expired")
    @Operation(summary = "清理过期座位锁", description = "清理所有过期的座位锁记录（定时任务调用）")
    public Result<Integer> cleanExpiredLocks() {
        log.info("手动触发清理过期座位锁");
        int count = seatLockService.cleanExpiredLocks();
        return Result.ok(count);
    }
}
