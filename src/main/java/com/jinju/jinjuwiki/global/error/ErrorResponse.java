package com.jinju.jinjuwiki.global.error;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

// 응답 DTO 클래스
@Getter
@Builder
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;
}
