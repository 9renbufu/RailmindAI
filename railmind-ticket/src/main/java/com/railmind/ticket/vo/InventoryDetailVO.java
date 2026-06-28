package com.railmind.ticket.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存详情")
public class InventoryDetailVO {

    @Schema(description = "车次ID")
    private Long trainId;

    @Schema(description = "车次号")
    private String trainNo;

    @Schema(description = "乘车日期")
    private LocalDate travelDate;

    @Schema(description = "各区间各座位类型库存")
    private List<RouteInventoryVO> routeInventories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "区间库存信息")
    public static class RouteInventoryVO {

        @Schema(description = "出发站编码")
        private String fromStation;

        @Schema(description = "出发站名称")
        private String fromStationName;

        @Schema(description = "到达站编码")
        private String toStation;

        @Schema(description = "到达站名称")
        private String toStationName;

        @Schema(description = "座位类型编码")
        private String seatTypeCode;

        @Schema(description = "座位类型名称")
        private String seatTypeName;

        @Schema(description = "总票数")
        private Integer totalCount;

        @Schema(description = "已售数量")
        private Integer soldCount;

        @Schema(description = "锁定中数量")
        private Integer lockedCount;

        @Schema(description = "剩余票数")
        private Integer remainCount;

        @Schema(description = "乐观锁版本")
        private Long version;
    }
}
