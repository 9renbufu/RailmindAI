package com.railmind.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private int pageNum;

    @Schema(description = "每页条数")
    private int pageSize;

    @Schema(description = "总页数")
    private int totalPages;

    @Schema(description = "数据列表")
    private List<T> records;

    public static <T> PageResult<T> of(long total, int pageNum, int pageSize, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotalPages(pageSize == 0 ? 0 : (int) Math.ceil((double) total / pageSize));
        result.setRecords(records);
        return result;
    }

    public static <T> PageResult<T> empty() {
        PageResult<T> result = new PageResult<>();
        result.setTotal(0);
        result.setPageNum(1);
        result.setPageSize(20);
        result.setTotalPages(0);
        result.setRecords(Collections.emptyList());
        return result;
    }
}
