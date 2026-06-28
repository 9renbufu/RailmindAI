package com.railmind.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单查询请求")
public class OrderQueryRequest {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "订单状态", example = "CREATED")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
