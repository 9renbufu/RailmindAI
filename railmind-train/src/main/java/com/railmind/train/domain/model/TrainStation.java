package com.railmind.train.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_train_station")
@Schema(description = "车次途经站实体")
public class TrainStation {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @TableField("train_id")
    @Schema(description = "车次ID")
    private Long trainId;

    @TableField("station_code")
    @Schema(description = "站点编码", example = "BJP")
    private String stationCode;

    @TableField("station_order")
    @Schema(description = "站序(1=始发站)")
    private Integer stationOrder;

    @TableField("arrival_time")
    @Schema(description = "到达时间(始发站为NULL)")
    private LocalTime arrivalTime;

    @TableField("departure_time")
    @Schema(description = "出发时间(终到站为NULL)")
    private LocalTime departureTime;

    @TableField("stop_duration")
    @Schema(description = "停靠时长(分钟)")
    @Builder.Default
    private Integer stopDuration = 0;

    @Schema(description = "从始发站起的里程(km)")
    private Integer mileage;
}
