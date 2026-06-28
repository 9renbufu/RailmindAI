package com.railmind.train.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "站点响应VO")
public class StationVO {

    @Schema(description = "站点ID")
    private Long id;

    @Schema(description = "站点编码", example = "BJP")
    private String code;

    @Schema(description = "站名", example = "北京南")
    private String name;

    @Schema(description = "所属城市", example = "北京")
    private String city;

    @Schema(description = "省份", example = "北京")
    private String province;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "所属路局", example = "北京局")
    private String bureau;
}
