package com.railmind.train.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
@Schema(description = "车次查询请求")
public class TrainQueryRequest {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 10;

    @Schema(description = "车次号(模糊查询)", example = "G1")
    private String trainNo;

    @Schema(description = "车次类型: G/D/C/Z/T/K/L")
    private String trainType;

    @Schema(description = "始发站编码")
    private String startStation;

    @Schema(description = "终到站编码")
    private String endStation;
}
