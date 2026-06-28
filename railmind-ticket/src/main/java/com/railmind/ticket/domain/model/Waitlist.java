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
@TableName("t_waitlist")
public class Waitlist {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long trainId;

    private LocalDate travelDate;

    private String fromStation;

    private String toStation;

    private String seatTypeCode;

    private String passengerIds;

    private Integer priority;

    private String status;

    private LocalDateTime expireAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
