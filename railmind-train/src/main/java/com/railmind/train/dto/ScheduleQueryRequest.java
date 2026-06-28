package com.railmind.train.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Schema(description = "运行图查询请求")
public class ScheduleQueryRequest {

    @NotBlank(message = "车次号不能为空")
    @Schema(description = "车次号", example = "G1")
    private String trainNo;

    @NotNull(message = "日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "运行日期", example = "2026-07-15")
    private LocalDate travelDate;
}
