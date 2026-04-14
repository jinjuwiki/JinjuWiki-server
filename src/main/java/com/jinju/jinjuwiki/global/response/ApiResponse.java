package com.jinju.jinjuwiki.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

// 성공/실패 공통 응답 DTO
@Getter
@Schema(description = "공통 성공 응답")
public class ApiResponse<T> {

    @Schema(description = "응답 메시지, 없는 경우 null", nullable = true, example = "문서가 생성되었습니다.")
    private final String message;

    @Schema(description = "응답 데이터 본문", nullable = true)
    private final T data;

    private ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    // 기본 성공 응답 생성 함수
    public static <T> ApiResponse<T> of(T data) {
        return success(data);
    }

    // 메시지 포함 성공 응답 생성 함수
    public static <T> ApiResponse<T> of(String message, T data) {
        return success(message, data);
    }

    // 데이터만 포함한 성공 응답 생성 함수
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(null, data);
    }

    // 메시지와 데이터를 포함한 성공 응답 생성 함수
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }

    // 메시지만 포함한 성공 응답 생성 함수
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(message, null);
    }

    // 메시지만 포함한 실패 응답 생성 함수
    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(message, null);
    }
}
