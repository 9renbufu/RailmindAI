package com.railmind.train.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "车次途经站响应VO")
public class TrainStationVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "站点编码", example = "BJP")
    private String stationCode;

    @Schema(description = "站名", example = "北京南")
    private String stationName;

    @Schema(description = "站序(1=始发站)")
    private Integer stationOrder;

    @Schema(description = "到达时间(始发站为NULL)")
    private LocalTime arrivalTime;

    @Schema(description = "出发时间(终到站为NULL)")
    private LocalTime departureTime;

    @Schema(description = "停靠时长(分钟)")
    private Integer stopDuration;

    @Schema(description = "从始发站起的里程(km)")
    private Integer mileage;
}
