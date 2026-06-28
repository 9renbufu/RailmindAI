package com.railmind.train.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "站点搜索请求")
public class StationSearchRequest {

    @NotBlank(message = "搜索关键词不能为空")
    @Size(min = 1, max = 20, message = "关键词长度1-20个字符")
    @Schema(description = "搜索关键词(站名/城市/编码)", example = "北京")
    private String keyword;
}
