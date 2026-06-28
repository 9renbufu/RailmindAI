package com.railmind.order.service.impl;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.order.mapper.OrderMapper;
import com.railmind.order.service.OrderStateMachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStateMachineServiceImpl implements OrderStateMachineService {

    private final OrderMapper orderMapper;

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "CREATED", Set.of("LOCKED", "CANCELLED"),
            "LOCKED", Set.of("PAID", "CANCELLED"),
            "PAID", Set.of("TICKETED", "REFUNDING"),
            "TICKETED", Set.of("COMPLETED", "REFUNDING"),
            "REFUNDING", Set.of("REFUNDED")
    );

    @Override
    public boolean canTransition(String currentStatus, String targetStatus) {
        Set<String> allowed = TRANSITIONS.get(currentStatus);
        return allowed != null && allowed.contains(targetStatus);
    }

    @Override
    public void transition(String orderNo, String currentStatus, String targetStatus) {
        if (!canTransition(currentStatus, targetStatus)) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR,
                    String.format("订单状态不允许从 %s 转换到 %s", currentStatus, targetStatus));
        }
    }

    @Override
    public void lockOrder(String orderNo) {
        int rows = orderMapper.updateStatus(orderNo, "CREATED", "LOCKED");
        if (rows == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR, "订单锁定失败，当前状态不允许");
        }
        log.info("订单状态变更: orderNo={}, CREATED→LOCKED", orderNo);
    }

    @Override
    public void payOrder(String orderNo) {
        int rows = orderMapper.payOrder(orderNo);
        if (rows == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR, "订单支付失败，当前状态不允许");
        }
        log.info("订单状态变更: orderNo={}, →PAID", orderNo);
    }

    @Override
    public void ticketOrder(String orderNo) {
        int rows = orderMapper.updateStatus(orderNo, "PAID", "TICKETED");
        if (rows == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR, "出票失败，当前状态不允许");
        }
        log.info("订单状态变更: orderNo={}, PAID→TICKETED", orderNo);
    }

    @Override
    public void completeOrder(String orderNo) {
        int rows = orderMapper.updateStatus(orderNo, "TICKETED", "COMPLETED");
        if (rows == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR, "完成订单失败，当前状态不允许");
        }
        log.info("订单状态变更: orderNo={}, TICKETED→COMPLETED", orderNo);
    }

    @Override
    public void cancelOrder(String orderNo, String reason) {
        int rows = orderMapper.cancelOrder(orderNo, reason);
        if (rows == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR, "取消订单失败，当前状态不允许");
        }
        log.info("订单状态变更: orderNo={}, →CANCELLED, reason={}", orderNo, reason);
    }
}
