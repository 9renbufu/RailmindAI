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
@Schema(description = "座位类型响应VO")
public class SeatTypeVO {

    @Schema(description = "座位编码", example = "ZE")
    private String seatTypeCode;

    @Schema(description = "座位名称", example = "二等座")
    private String seatTypeName;

    @Schema(description = "总座位数")
    private Integer totalCount;

    @Schema(description = "票价系数")
    private BigDecimal priceFactor;
}
