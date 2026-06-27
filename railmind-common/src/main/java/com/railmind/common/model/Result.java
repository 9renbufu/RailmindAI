package com.railmind.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {

    @Schema(description = "状态码", example = "200")
    private int code;

    @Schema(description = "提示信息", example = "success")
    private String message;

    @Schema(description = "响应数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @Schema(description = "请求唯一标识")
    private String traceId;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}
