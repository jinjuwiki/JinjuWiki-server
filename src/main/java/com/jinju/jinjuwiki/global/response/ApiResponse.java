package com.jinju.jinjuwiki.global.response;

import lombok.Getter;

// 성공시, 응답 규격을 맞추기 위한 DTO
@Getter
public class ApiResponse<T> {

    private final T data;

    private ApiResponse(T data) {
        this.data = data;
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}
