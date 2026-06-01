package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {
    private T data;
    private ApiError error;

    public ApiResponse() {}

    public ApiResponse(T data, ApiError error) {
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(null, new ApiError(code, message));
    }

    // Getters and Setters
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public ApiError getError() { return error; }
    public void setError(ApiError error) { this.error = error; }
}