package com.railmind.ticket.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "余票查询结果")
public class TicketQueryVO {

    @Schema(description = "车次ID")
    private Long trainId;

    @Schema(description = "车次号", example = "G1")
    private String trainNo;

    @Schema(description = "车次类型", example = "G")
    private String trainType;

    @Schema(description = "车次类型名称", example = "高铁")
    private String trainTypeName;

    @Schema(description = "出发站编码", example = "BJN")
    private String fromStation;

    @Schema(description = "出发站名称", example = "北京南站")
    private String fromStationName;

    @Schema(description = "到达站编码", example = "AOH")
    private String toStation;

    @Schema(description = "到达站名称", example = "上海虹桥站")
    private String toStationName;

    @Schema(description = "乘车日期")
    private LocalDate travelDate;

    @Schema(description = "出发时间", example = "09:00")
    private LocalTime departureTime;

    @Schema(description = "到达时间", example = "13:28")
    private LocalTime arrivalTime;

    @Schema(description = "运行天数", example = "1")
    private Integer runDays;

    @Schema(description = "里程(km)", example = "1318")
    private Integer mileage;

    @Schema(description = "各座位类型余票信息")
    private List<SeatTypeTicketVO> seatTickets;
}
