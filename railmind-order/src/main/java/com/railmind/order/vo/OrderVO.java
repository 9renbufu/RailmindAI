package com.railmind.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单详情VO")
public class OrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "车次ID")
    private Long trainId;

    @Schema(description = "车次号")
    private String trainNo;

    @Schema(description = "乘车日期")
    private LocalDate travelDate;

    @Schema(description = "出发站编码")
    private String fromStation;

    @Schema(description = "出发站名")
    private String fromStationName;

    @Schema(description = "到达站编码")
    private String toStation;

    @Schema(description = "到达站名")
    private String toStationName;

    @Schema(description = "发车时间")
    private LocalTime departureTime;

    @Schema(description = "到达时间")
    private LocalTime arrivalTime;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "支付截止时间")
    private LocalDateTime payDeadline;

    @Schema(description = "支付完成时间")
    private LocalDateTime paidAt;

    @Schema(description = "取消时间")
    private LocalDateTime cancelledAt;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "订单明细列表")
    private List<OrderItemVO> items;
}
