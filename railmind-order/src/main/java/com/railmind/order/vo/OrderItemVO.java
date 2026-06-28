package com.railmind.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单明细VO")
public class OrderItemVO {

    @Schema(description = "明细ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "乘车人ID")
    private Long passengerId;

    @Schema(description = "乘车人姓名")
    private String passengerName;

    @Schema(description = "座位类型编码")
    private String seatTypeCode;

    @Schema(description = "座位类型名称")
    private String seatTypeName;

    @Schema(description = "座位号")
    private String seatNo;

    @Schema(description = "票价")
    private BigDecimal ticketPrice;

    @Schema(description = "状态: NORMAL/REFUNDED/CHANGED")
    private String status;
}
