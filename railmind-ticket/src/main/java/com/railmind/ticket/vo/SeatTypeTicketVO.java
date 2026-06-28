package com.railmind.ticket.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "座位类型余票信息")
public class SeatTypeTicketVO {

    @Schema(description = "座位类型编码", example = "ZE")
    private String seatTypeCode;

    @Schema(description = "座位类型名称", example = "二等座")
    private String seatTypeName;

    @Schema(description = "票价(元)", example = "553.00")
    private BigDecimal price;

    @Schema(description = "总票数", example = "868")
    private Integer totalCount;

    @Schema(description = "已售数量", example = "100")
    private Integer soldCount;

    @Schema(description = "锁定中数量", example = "10")
    private Integer lockedCount;

    @Schema(description = "剩余票数", example = "758")
    private Integer remainCount;
}
