package com.railmind.train.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "车次响应VO")
public class TrainVO {

    @Schema(description = "车次ID")
    private Long id;

    @Schema(description = "车次号", example = "G1")
    private String trainNo;

    @Schema(description = "车次类型: G-高铁 D-动车 C-城际 Z-直达 T-特快 K-快速")
    private String trainType;

    @Schema(description = "车次类型名称", example = "高铁")
    private String trainTypeName;

    @Schema(description = "始发站编码")
    private String startStation;

    @Schema(description = "始发站名称")
    private String startStationName;

    @Schema(description = "终到站编码")
    private String endStation;

    @Schema(description = "终到站名称")
    private String endStationName;

    @Schema(description = "发车时间")
    private LocalTime departureTime;

    @Schema(description = "到达时间")
    private LocalTime arrivalTime;

    @Schema(description = "运行天数")
    private Integer runDays;

    @Schema(description = "总里程(km)")
    private Integer totalMileage;

    @Schema(description = "状态: 0-停运 1-正常")
    private Integer status;

    @Schema(description = "途经站列表")
    private List<TrainStationVO> stations;

    @Schema(description = "座位类型列表")
    private List<SeatTypeVO> seatTypes;
}
