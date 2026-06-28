package com.railmind.order.domain.service;

import com.railmind.common.util.IdGenerator;
import com.railmind.order.domain.model.Order;
import com.railmind.order.domain.model.OrderItem;
import com.railmind.order.dto.OrderCreateRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderDomainService {

    private static final int PAY_DEADLINE_MINUTES = 15;

    public Order createOrder(OrderCreateRequest request) {
        String orderNo = IdGenerator.generateOrderNo();
        BigDecimal totalAmount = calculateTotalAmount(request.getPassengers());
        LocalDateTime payDeadline = LocalDateTime.now().plusMinutes(PAY_DEADLINE_MINUTES);

        return Order.builder()
                .orderNo(orderNo)
                .userId(request.getUserId())
                .trainId(request.getTrainId())
                .trainNo(request.getTrainNo())
                .travelDate(request.getTravelDate())
                .fromStation(request.getFromStation())
                .fromStationName(request.getFromStationName())
                .toStation(request.getToStation())
                .toStationName(request.getToStationName())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .totalAmount(totalAmount)
                .status("CREATED")
                .payDeadline(payDeadline)
                .deleted(0)
                .build();
    }

    public List<OrderItem> createOrderItems(Order order, OrderCreateRequest request) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderCreateRequest.PassengerInfo passenger : request.getPassengers()) {
            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .passengerId(passenger.getPassengerId())
                    .passengerName(passenger.getPassengerName())
                    .idCard(passenger.getIdCard())
                    .idCardHash(passenger.getIdCardHash())
                    .seatTypeCode(passenger.getSeatTypeCode())
                    .seatTypeName(passenger.getSeatTypeName())
                    .seatNo(passenger.getSeatNo())
                    .ticketPrice(passenger.getTicketPrice())
                    .status("NORMAL")
                    .build();
            items.add(item);
        }
        return items;
    }

    public BigDecimal calculateTotalAmount(List<OrderCreateRequest.PassengerInfo> passengers) {
        return passengers.stream()
                .map(OrderCreateRequest.PassengerInfo::getTicketPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isExpired(Order order) {
        return "CREATED".equals(order.getStatus())
                && order.getPayDeadline() != null
                && LocalDateTime.now().isAfter(order.getPayDeadline());
    }

    public boolean canCancel(Order order) {
        return "CREATED".equals(order.getStatus());
    }
}
