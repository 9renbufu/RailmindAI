package com.railmind.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.common.model.PageResult;
import com.railmind.order.domain.event.OrderCreatedEvent;
import com.railmind.order.domain.model.Order;
import com.railmind.order.domain.model.OrderItem;
import com.railmind.order.domain.service.OrderDomainService;
import com.railmind.order.dto.OrderCreateRequest;
import com.railmind.order.dto.OrderQueryRequest;
import com.railmind.order.mapper.OrderItemMapper;
import com.railmind.order.mapper.OrderMapper;
import com.railmind.order.producer.OrderEventProducer;
import com.railmind.order.service.OrderService;
import com.railmind.order.vo.OrderItemVO;
import com.railmind.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderDomainService orderDomainService;
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderCreateRequest request) {
        Order existingOrder = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, request.getUserId())
                        .eq(Order::getTrainId, request.getTrainId())
                        .eq(Order::getTravelDate, request.getTravelDate())
                        .eq(Order::getFromStation, request.getFromStation())
                        .eq(Order::getToStation, request.getToStation())
                        .in(Order::getStatus, "CREATED", "LOCKED", "PAID")
                        .last("LIMIT 1")
        );
        if (existingOrder != null) {
            log.warn("用户重复下单: userId={}, trainId={}, orderNo={}",
                    request.getUserId(), request.getTrainId(), existingOrder.getOrderNo());
            throw new BizException(ErrorCode.ORDER_DUPLICATE, "您已有相同行程的待支付订单: " + existingOrder.getOrderNo());
        }

        Order order = orderDomainService.createOrder(request);
        orderMapper.insert(order);
        log.info("订单创建成功: orderNo={}, userId={}, trainNo={}", order.getOrderNo(), order.getUserId(), order.getTrainNo());

        List<OrderItem> items = orderDomainService.createOrderItems(order, request);
        for (OrderItem item : items) {
            orderItemMapper.insert(item);
        }
        log.info("订单明细创建成功: orderNo={}, count={}", order.getOrderNo(), items.size());

        OrderCreatedEvent event = buildOrderCreatedEvent(order, items);
        orderEventProducer.sendOrderCreatedEvent(event);

        return toOrderVO(order, items);
    }

    @Override
    public OrderVO getOrderDetail(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderNo);
        }
        List<OrderItem> items = orderItemMapper.selectByOrderNo(orderNo);
        return toOrderVO(order, items);
    }

    @Override
    public PageResult<OrderVO> queryOrders(OrderQueryRequest request) {
        Page<Order> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (request.getUserId() != null) {
            wrapper.eq(Order::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(Order::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> result = orderMapper.selectPage(page, wrapper);
        List<OrderVO> voList = result.getRecords().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemMapper.selectByOrderNo(order.getOrderNo());
                    return toOrderVO(order, items);
                })
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), request.getPageNum(), request.getPageSize(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancelOrder(String orderNo, Long userId, String reason) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderNo);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作此订单");
        }
        if (!orderDomainService.canCancel(order)) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR, "当前订单状态不允许取消");
        }

        orderMapper.cancelOrder(orderNo, reason);
        log.info("订单取消成功: orderNo={}, reason={}", orderNo, reason);

        Order cancelledOrder = orderMapper.selectByOrderNo(orderNo);
        List<OrderItem> items = orderItemMapper.selectByOrderNo(orderNo);
        return toOrderVO(cancelledOrder, items);
    }

    @Override
    public String getOrderStatus(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderNo);
        }
        return order.getStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleExpiredOrders() {
        List<Order> expiredOrders = orderMapper.selectExpiredOrders(LocalDateTime.now());
        log.info("扫描到过期订单: count={}", expiredOrders.size());
        for (Order order : expiredOrders) {
            try {
                orderMapper.cancelOrder(order.getOrderNo(), "支付超时自动取消");
                log.info("过期订单自动取消: orderNo={}", order.getOrderNo());
            } catch (Exception e) {
                log.error("过期订单取消失败: orderNo={}", order.getOrderNo(), e);
            }
        }
    }

    private OrderVO toOrderVO(Order order, List<OrderItem> items) {
        List<OrderItemVO> itemVOs = items.stream()
                .map(this::toOrderItemVO)
                .collect(Collectors.toList());
        return OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .trainId(order.getTrainId())
                .trainNo(order.getTrainNo())
                .travelDate(order.getTravelDate())
                .fromStation(order.getFromStation())
                .fromStationName(order.getFromStationName())
                .toStation(order.getToStation())
                .toStationName(order.getToStationName())
                .departureTime(order.getDepartureTime())
                .arrivalTime(order.getArrivalTime())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .payDeadline(order.getPayDeadline())
                .paidAt(order.getPaidAt())
                .cancelledAt(order.getCancelledAt())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .items(itemVOs)
                .build();
    }

    private OrderItemVO toOrderItemVO(OrderItem item) {
        return OrderItemVO.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .orderNo(item.getOrderNo())
                .passengerId(item.getPassengerId())
                .passengerName(item.getPassengerName())
                .seatTypeCode(item.getSeatTypeCode())
                .seatTypeName(item.getSeatTypeName())
                .seatNo(item.getSeatNo())
                .ticketPrice(item.getTicketPrice())
                .status(item.getStatus())
                .build();
    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order, List<OrderItem> items) {
        List<OrderCreatedEvent.OrderItemInfo> itemInfos = items.stream()
                .map(item -> OrderCreatedEvent.OrderItemInfo.builder()
                        .passengerId(item.getPassengerId())
                        .passengerName(item.getPassengerName())
                        .seatTypeCode(item.getSeatTypeCode())
                        .seatTypeName(item.getSeatTypeName())
                        .seatNo(item.getSeatNo())
                        .ticketPrice(item.getTicketPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderCreatedEvent.builder()
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .trainId(order.getTrainId())
                .trainNo(order.getTrainNo())
                .travelDate(order.getTravelDate())
                .fromStation(order.getFromStation())
                .fromStationName(order.getFromStationName())
                .toStation(order.getToStation())
                .toStationName(order.getToStationName())
                .departureTime(order.getDepartureTime())
                .arrivalTime(order.getArrivalTime())
                .totalAmount(order.getTotalAmount())
                .payDeadline(order.getPayDeadline())
                .items(itemInfos)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
