package com.railmind.train.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "票价响应VO")
public class PriceVO {

    @Schema(description = "车次号", example = "G1")
    private String trainNo;

    @Schema(description = "出发站编码", example = "BJP")
    private String fromStation;

    @Schema(description = "出发站名称", example = "北京南")
    private String fromStationName;

    @Schema(description = "到达站编码", example = "SHH")
    private String toStation;

    @Schema(description = "到达站名称", example = "上海虹桥")
    private String toStationName;

    @Schema(description = "区间里程(km)")
    private Integer mileage;

    @Schema(description = "各座位类型票价列表")
    private List<SeatPriceVO> seatPrices;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "座位票价详情")
    public static class SeatPriceVO {

        @Schema(description = "座位编码", example = "ZE")
        private String seatTypeCode;

        @Schema(description = "座位名称", example = "二等座")
        private String seatTypeName;

        @Schema(description = "票价(元)")
        private BigDecimal price;
    }
}
