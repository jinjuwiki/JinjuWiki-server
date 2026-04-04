package com.jinju.jinjuwiki.global.error;

import lombok.Getter;

// 정상적인 범위 내 생길 수 있는 에러(의도적)
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
