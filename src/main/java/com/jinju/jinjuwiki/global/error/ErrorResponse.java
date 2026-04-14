package com.jinju.jinjuwiki.global.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

// 응답 DTO 클래스
@Getter
@Builder
@Schema(description = "공통 에러 응답")
public class ErrorResponse {

    @Schema(description = "에러 발생 시각", example = "2026-04-14T10:15:30")
    private final LocalDateTime timestamp;

    @Schema(description = "HTTP 상태 코드", example = "400")
    private final int status;

    @Schema(description = "서비스 내부 에러 코드", example = "INVALID_INPUT")
    private final String code;

    @Schema(description = "클라이언트에 노출할 에러 메시지", example = "잘못된 요청입니다.")
    private final String message;

    @Schema(description = "에러가 발생한 요청 경로", example = "/api/documents")
    private final String path;
}
