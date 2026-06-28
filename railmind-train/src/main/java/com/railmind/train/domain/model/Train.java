package com.railmind.train.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_train")
@Schema(description = "车次实体")
public class Train {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "车次ID")
    private Long id;

    @TableField("train_no")
    @Schema(description = "车次号", example = "G1")
    private String trainNo;

    @TableField("train_type")
    @Schema(description = "车次类型: G-高铁 D-动车 C-城际 Z-直达 T-特快 K-快速")
    private String trainType;

    @TableField("start_station")
    @Schema(description = "始发站编码", example = "BJP")
    private String startStation;

    @TableField("end_station")
    @Schema(description = "终到站编码", example = "SHH")
    private String endStation;

    @TableField("departure_time")
    @Schema(description = "发车时间")
    private LocalTime departureTime;

    @TableField("arrival_time")
    @Schema(description = "到达时间")
    private LocalTime arrivalTime;

    @TableField("run_days")
    @Schema(description = "运行天数(跨天运行>1)")
    @Builder.Default
    private Integer runDays = 1;

    @TableField("total_mileage")
    @Schema(description = "总里程(km)")
    private Integer totalMileage;

    @Schema(description = "状态: 0-停运 1-正常")
    @Builder.Default
    private Integer status = 1;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableLogic
    @Builder.Default
    private Integer deleted = 0;
}
