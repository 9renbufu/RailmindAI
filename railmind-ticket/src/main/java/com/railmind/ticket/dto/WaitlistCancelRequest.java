package com.railmind.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "取消候补请求")
public class WaitlistCancelRequest {

    @NotNull(message = "候补ID不能为空")
    @Schema(description = "候补ID", example = "1")
    private Long waitlistId;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;
}
