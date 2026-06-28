package com.railmind.train.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "运行图响应VO")
public class ScheduleVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "车次ID")
    private Long trainId;

    @Schema(description = "车次号", example = "G1")
    private String trainNo;

    @Schema(description = "运行日期", example = "2026-07-15")
    private LocalDate travelDate;

    @Schema(description = "状态: 0-停运 1-正常 2-加开")
    private Integer status;

    @Schema(description = "状态名称", example = "正常")
    private String statusName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
