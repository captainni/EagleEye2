package com.eagleeye.model.vo;

import lombok.Data;

/**
 * API响应视图对象
 */
@Data
public class ApiResponseVO<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    public ApiResponseVO() {}

    public ApiResponseVO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponseVO<T> success(String message, T data) {
        return new ApiResponseVO<>(200, message, data);
    }

    public static <T> ApiResponseVO<T> success(T data) {
        return new ApiResponseVO<>(200, "成功", data);
    }

    public static <T> ApiResponseVO<T> error(String message) {
        return new ApiResponseVO<>(500, message, null);
    }
}
