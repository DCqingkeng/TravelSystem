package com.travel.entity.dto;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一API响应结果封装
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Map<String, Object> extra = new HashMap<>();

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> r = success(data);
        r.setMessage(message);
        return r;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public Result<T> setExtra(String key, Object value) {
        this.extra.put(key, value);
        return this;
    }
}