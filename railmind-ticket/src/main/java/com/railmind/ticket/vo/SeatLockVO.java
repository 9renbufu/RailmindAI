package com.railmind.ticket.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "座位锁定信息")
public class SeatLockVO {

    @Schema(description = "锁定ID", example = "1")
    private Long id;

    @Schema(description = "车次ID", example = "1")
    private Long trainId;

    @Schema(description = "乘车日期", example = "2026-07-01")
    private LocalDate travelDate;

    @Schema(description = "座位类型编码", example = "SW")
    private String seatTypeCode;

    @Schema(description = "座位号", example = "05车12A")
    private String seatNo;

    @Schema(description = "关联订单号")
    private String orderNo;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "锁定时间")
    private LocalDateTime lockTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "状态: 0-释放 1-锁定", example = "1")
    private Integer status;

    @Schema(description = "剩余锁定时间(秒)")
    private Long remainingSeconds;
}
