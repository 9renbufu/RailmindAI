package com.railmind.order.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_outbox")
public class Outbox {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String aggregateType;

    private String aggregateId;

    private String eventType;

    private String payload;

    private String status;

    private Integer retryCount;

    private LocalDateTime nextRetryAt;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
