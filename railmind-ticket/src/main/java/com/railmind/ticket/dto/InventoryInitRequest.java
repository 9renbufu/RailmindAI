package com.railmind.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "库存初始化请求")
public class InventoryInitRequest {

    @NotNull(message = "车次ID不能为空")
    @Schema(description = "车次ID")
    private Long trainId;

    @NotNull(message = "乘车日期不能为空")
    @Schema(description = "乘车日期")
    private LocalDate travelDate;

    @Schema(description = "出发站编码(为空则初始化全部区间)")
    private String fromStation;

    @Schema(description = "到达站编码(为空则初始化全部区间)")
    private String toStation;
}
