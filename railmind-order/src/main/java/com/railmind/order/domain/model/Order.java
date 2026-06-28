package com.railmind.order.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    private String status;

    private LocalDateTime payDeadline;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    private String cancelReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
