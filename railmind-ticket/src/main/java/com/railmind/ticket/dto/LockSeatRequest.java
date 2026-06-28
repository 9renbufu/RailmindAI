package com.railmind.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "锁座请求")
public class LockSeatRequest {

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

    @Schema(description = "指定座位号", example = "05车12A")
    private String seatNo;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "关联订单号")
    private String orderNo;

    @Min(value = 1, message = "锁定时长至少1分钟")
    @Schema(description = "锁定时长(分钟)", example = "15")
    private Integer lockMinutes = 15;
}
