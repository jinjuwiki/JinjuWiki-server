package com.jinju.jinjuwiki.global.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 해당 클래스에서 모든 에러 처리
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 전역 예외 로깅용 로거
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 의도적 에러 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        return buildResponse(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
    }

    // @Valid 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError == null ? ErrorCode.INVALID_INPUT.getMessage() : fieldError.getDefaultMessage();

        return buildResponse(ErrorCode.INVALID_INPUT, message, request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ErrorCode.INVALID_INPUT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getMostSpecificCause() == null ? "" : ex.getMostSpecificCause().getMessage();

        if (message.contains("email")) {
            return buildResponse(
                    ErrorCode.DUPLICATE_EMAIL,
                    ErrorCode.DUPLICATE_EMAIL.getMessage(),
                    request.getRequestURI()
            );
        }

        if (message.contains("nickname")) {
            return buildResponse(
                    ErrorCode.DUPLICATE_NICKNAME,
                    ErrorCode.DUPLICATE_NICKNAME.getMessage(),
                    request.getRequestURI()
            );
        }

        return buildResponse(
                ErrorCode.INVALID_INPUT,
                ErrorCode.INVALID_INPUT.getMessage(),
                request.getRequestURI()
        );
    }

    // 서버 터짐 방지
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        // 예상하지 못한 서버 예외 로깅
        log.error("Unhandled exception. path={}", request.getRequestURI(), ex);
        return buildResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode, String message, String path) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
