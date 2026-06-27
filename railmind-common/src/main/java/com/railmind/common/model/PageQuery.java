package com.railmind.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "分页查询参数")
public class PageQuery implements Serializable {

    @Schema(description = "页码(从1开始)", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private int pageNum = 1;

    @Schema(description = "每页条数", example = "20")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private int pageSize = 20;

    @Schema(description = "排序字段")
    private String orderBy;

    @Schema(description = "是否降序", example = "true")
    private boolean desc = true;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
