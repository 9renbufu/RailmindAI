package com.railmind.order.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {

    private String orderNo;
    private Long userId;
    private Long trainId;
    private String trainNo;
    private LocalDate travelDate;
    private String fromStation;
    private String fromStationName;
    private String toStation;
    private String toStationName;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private BigDecimal totalAmount;
    private LocalDateTime payDeadline;
    private List<OrderItemInfo> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo implements Serializable {
        private Long passengerId;
        private String passengerName;
        private String seatTypeCode;
        private String seatTypeName;
        private String seatNo;
        private BigDecimal ticketPrice;
    }
}
