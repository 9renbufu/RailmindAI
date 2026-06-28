package com.railmind.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "批量余票查询请求")
public class BatchQueryRequest {

    @NotNull(message = "乘车日期不能为空")
    @Schema(description = "乘车日期")
    private LocalDate travelDate;

    @NotBlank(message = "出发站不能为空")
    @Schema(description = "出发站编码", example = "BJN")
    private String fromStation;

    @NotBlank(message = "到达站不能为空")
    @Schema(description = "到达站编码", example = "AOH")
    private String toStation;

    @Size(min = 1, max = 10, message = "车次号数量1-10个")
    @Schema(description = "车次号列表")
    private List<String> trainNos;
}
