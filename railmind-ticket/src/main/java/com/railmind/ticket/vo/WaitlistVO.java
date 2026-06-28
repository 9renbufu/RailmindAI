package com.railmind.ticket.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "候补购票信息")
public class WaitlistVO {

    @Schema(description = "候补ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "车次ID", example = "1")
    private Long trainId;

    @Schema(description = "乘车日期", example = "2026-07-01")
    private LocalDate travelDate;

    @Schema(description = "出发站编码", example = "BJP")
    private String fromStation;

    @Schema(description = "到达站编码", example = "SHH")
    private String toStation;

    @Schema(description = "座位类型编码", example = "SW")
    private String seatTypeCode;

    @Schema(description = "乘车人ID(JSON数组)", example = "[1,2]")
    private String passengerIds;

    @Schema(description = "优先级", example = "100")
    private Integer priority;

    @Schema(description = "状态: WAITING/FULFILLED/CANCELLED", example = "WAITING")
    private String status;

    @Schema(description = "过期时间")
    private LocalDateTime expireAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
