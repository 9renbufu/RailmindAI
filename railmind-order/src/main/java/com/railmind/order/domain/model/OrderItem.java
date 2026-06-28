package com.railmind.order.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_item")
public class OrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String orderNo;

    private Long passengerId;

    private String passengerName;

    private String idCard;

    private String idCardHash;

    private String seatTypeCode;

    private String seatTypeName;

    private String seatNo;

    private BigDecimal ticketPrice;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
