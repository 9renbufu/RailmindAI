package com.railmind.order.controller;

import com.railmind.common.model.PageResult;
import com.railmind.common.model.Result;
import com.railmind.order.dto.OrderCreateRequest;
import com.railmind.order.dto.OrderQueryRequest;
import com.railmind.order.service.OrderService;
import com.railmind.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单管理", description = "订单创建、查询、取消、状态管理")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderVO order = orderService.createOrder(request);
        return Result.ok(order);
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderVO> getOrderDetail(
            @Parameter(description = "订单号") @PathVariable String orderNo) {
        OrderVO order = orderService.getOrderDetail(orderNo);
        return Result.ok(order);
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> queryOrders(OrderQueryRequest request) {
        PageResult<OrderVO> result = orderService.queryOrders(request);
        return Result.ok(result);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderNo}/cancel")
    public Result<OrderVO> cancelOrder(
            @Parameter(description = "订单号") @PathVariable String orderNo,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "取消原因") @RequestParam(required = false, defaultValue = "用户主动取消") String reason) {
        OrderVO order = orderService.cancelOrder(orderNo, userId, reason);
        return Result.ok(order);
    }

    @Operation(summary = "查询订单状态")
    @GetMapping("/{orderNo}/status")
    public Result<String> getOrderStatus(
            @Parameter(description = "订单号") @PathVariable String orderNo) {
        String status = orderService.getOrderStatus(orderNo);
        return Result.ok(status);
    }
}
