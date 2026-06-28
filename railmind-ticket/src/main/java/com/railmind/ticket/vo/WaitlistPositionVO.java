package com.railmind.ticket.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "候补排队位置信息")
public class WaitlistPositionVO {

    @Schema(description = "候补ID", example = "1")
    private Long waitlistId;

    @Schema(description = "当前排队位置", example = "5")
    private Integer position;

    @Schema(description = "前面等待人数", example = "4")
    private Integer aheadCount;

    @Schema(description = "预估等待时间(分钟)", example = "30")
    private Integer estimatedWaitMinutes;

    @Schema(description = "候补状态", example = "WAITING")
    private String status;
}
