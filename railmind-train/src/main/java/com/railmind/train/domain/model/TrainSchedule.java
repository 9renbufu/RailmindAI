package com.railmind.train.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_train_schedule")
@Schema(description = "车次运行图实体")
public class TrainSchedule {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @TableField("train_id")
    @Schema(description = "车次ID")
    private Long trainId;

    @TableField("travel_date")
    @Schema(description = "运行日期")
    private LocalDate travelDate;

    @Schema(description = "状态: 0-停运 1-正常 2-加开")
    @Builder.Default
    private Integer status = 1;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
