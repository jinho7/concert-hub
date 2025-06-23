package com.concerthub.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> onSuccess(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> onFailure(String code, String message, T data) {
        return new ApiResponse<>(false, data, message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}