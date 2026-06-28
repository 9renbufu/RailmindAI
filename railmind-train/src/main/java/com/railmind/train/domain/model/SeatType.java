package com.railmind.train.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_seat_type")
@Schema(description = "座位类型实体")
public class SeatType {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @TableField("train_id")
    @Schema(description = "车次ID")
    private Long trainId;

    @TableField("seat_type_code")
    @Schema(description = "座位编码", example = "ZE")
    private String seatTypeCode;

    @TableField("seat_type_name")
    @Schema(description = "座位名称", example = "二等座")
    private String seatTypeName;

    @TableField("total_count")
    @Schema(description = "总座位数")
    private Integer totalCount;

    @TableField("price_factor")
    @Schema(description = "票价系数(相对二等座)")
    private BigDecimal priceFactor;
}
