package com.jinju.jinjuwiki.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 모든 에러를 관리하여 에러 발생시 처리 표준화 Enum
@Getter
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "잘못된 요청입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다."),
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL_VERIFICATION_NOT_FOUND", "이메일 인증 요청을 찾을 수 없습니다."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_CODE_MISMATCH", "인증코드가 올바르지 않습니다."),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_EXPIRED", "인증코드가 만료되었습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED", "인증 메일 발송에 실패했습니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", "현재 비밀번호가 올바르지 않습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "SAME_AS_OLD_PASSWORD", "새 비밀번호는 현재 비밀번호와 달라야 합니다."),
    EMPTY_UPLOAD_FILE(HttpStatus.BAD_REQUEST, "EMPTY_UPLOAD_FILE", "업로드할 이미지 파일이 비어 있습니다."),
    INVALID_UPLOAD_FILE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_UPLOAD_FILE_TYPE", "지원하지 않는 이미지 형식입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."),
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "문서를 찾을 수 없습니다."),
    FORBIDDEN_DOCUMENT_ACCESS(HttpStatus.FORBIDDEN, "FORBIDDEN_DOCUMENT_ACCESS", "문서에 접근할 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
