package com.railmind.ticket.domain.model;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("t_ticket_inventory")
public class TicketInventory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long trainId;

    private LocalDate travelDate;

    private String fromStation;

    private String toStation;

    private String seatTypeCode;

    private Integer totalCount;

    private Integer soldCount;

    private Integer lockedCount;

    @Version
    private Long version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
