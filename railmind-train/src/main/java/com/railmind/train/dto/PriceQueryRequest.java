package com.railmind.train.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "票价查询请求")
public class PriceQueryRequest {

    @NotBlank(message = "车次号不能为空")
    @Schema(description = "车次号", example = "G1")
    private String trainNo;

    @NotBlank(message = "出发站不能为空")
    @Schema(description = "出发站编码", example = "BJP")
    private String fromStation;

    @NotBlank(message = "到达站不能为空")
    @Schema(description = "到达站编码", example = "SHH")
    private String toStation;
}
