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
@TableName("t_ticket_price")
@Schema(description = "票价实体")
public class TicketPrice {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @TableField("train_id")
    @Schema(description = "车次ID")
    private Long trainId;

    @TableField("from_station")
    @Schema(description = "出发站编码", example = "BJP")
    private String fromStation;

    @TableField("to_station")
    @Schema(description = "到达站编码", example = "SHH")
    private String toStation;

    @TableField("seat_type_code")
    @Schema(description = "座位类型", example = "ZE")
    private String seatTypeCode;

    @Schema(description = "票价(元)")
    private BigDecimal price;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
