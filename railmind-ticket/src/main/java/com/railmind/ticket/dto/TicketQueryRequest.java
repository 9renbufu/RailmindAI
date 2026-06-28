package com.railmind.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "余票查询请求")
public class TicketQueryRequest {

    @NotBlank(message = "车次号不能为空")
    @Schema(description = "车次号", example = "G1")
    private String trainNo;

    @NotNull(message = "乘车日期不能为空")
    @Schema(description = "乘车日期")
    private LocalDate travelDate;

    @NotBlank(message = "出发站不能为空")
    @Schema(description = "出发站编码", example = "BJN")
    private String fromStation;

    @NotBlank(message = "到达站不能为空")
    @Schema(description = "到达站编码", example = "AOH")
    private String toStation;
}
