package com.railmind.ticket.controller;

import com.railmind.common.model.Result;
import com.railmind.ticket.dto.InventoryInitRequest;
import com.railmind.ticket.service.InventoryService;
import com.railmind.ticket.vo.InventoryDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "库存管理", description = "票务库存管理接口")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/init")
    @Operation(summary = "初始化库存", description = "给运行计划初始化库存，支持指定区间或全部区间")
    public Result<Void> initInventory(
            @Valid @RequestBody InventoryInitRequest request) {
        log.info("库存初始化请求: trainId={}, date={}", request.getTrainId(), request.getTravelDate());
        inventoryService.initInventory(request);
        return Result.ok();
    }

    @GetMapping("/detail")
    @Operation(summary = "库存详情", description = "查询某车次所有区间库存")
    public Result<InventoryDetailVO> getInventoryDetail(
            @Parameter(description = "车次ID", required = true) @RequestParam Long trainId,
            @Parameter(description = "乘车日期", required = true) @RequestParam String travelDate) {
        log.info("库存详情查询: trainId={}, date={}", trainId, travelDate);
        InventoryDetailVO result = inventoryService.getInventoryDetail(trainId, travelDate);
        return Result.ok(result);
    }
}
