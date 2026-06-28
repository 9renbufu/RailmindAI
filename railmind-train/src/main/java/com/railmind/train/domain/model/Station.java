package com.railmind.train.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_station")
@Schema(description = "站点实体")
public class Station {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "站点ID")
    private Long id;

    @Schema(description = "站点编码", example = "BJP")
    private String code;

    @Schema(description = "站名", example = "北京南")
    private String name;

    @Schema(description = "所属城市", example = "北京")
    private String city;

    @Schema(description = "省份", example = "北京")
    private String province;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "所属路局", example = "北京局")
    private String bureau;

    @Schema(description = "状态: 0-停用 1-正常")
    @Builder.Default
    private Integer status = 1;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
