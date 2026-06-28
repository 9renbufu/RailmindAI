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
@TableName("t_seat_lock")
public class SeatLock {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long trainId;

    private LocalDate travelDate;

    private String seatTypeCode;

    private String seatNo;

    private String orderNo;

    private Long userId;

    private LocalDateTime lockTime;

    private LocalDateTime expireTime;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
