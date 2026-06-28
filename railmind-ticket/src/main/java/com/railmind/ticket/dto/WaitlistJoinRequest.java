package com.railmind.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "候补购票请求")
public class WaitlistJoinRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotNull(message = "车次ID不能为空")
    @Schema(description = "车次ID", example = "1")
    private Long trainId;

    @NotNull(message = "乘车日期不能为空")
    @Schema(description = "乘车日期", example = "2026-07-01")
    private LocalDate travelDate;

    @NotBlank(message = "出发站不能为空")
    @Schema(description = "出发站编码", example = "BJP")
    private String fromStation;

    @NotBlank(message = "到达站不能为空")
    @Schema(description = "到达站编码", example = "SHH")
    private String toStation;

    @NotBlank(message = "座位类型不能为空")
    @Schema(description = "座位类型编码", example = "SW")
    private String seatTypeCode;

    @NotBlank(message = "乘车人ID不能为空")
    @Schema(description = "乘车人ID(JSON数组)", example = "[1,2]")
    private String passengerIds;
}
