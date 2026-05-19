package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.config.RedisCounterSupport;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 비밀번호 재설정 요청 제한 Redis 클래스
@Component
@RequiredArgsConstructor
public class RedisPasswordResetRequestRateLimiter {

    private static final String RESET_LIMIT_KEY_PREFIX = "auth:password-reset";

    private final RedisCounterSupport redisCounterSupport;

    @Value("${app.auth.password-reset-request-rate-limit.count:5}")
    private long resetLimitCount;

    @Value("${app.auth.password-reset-request-rate-limit.window-seconds:300}")
    private long resetLimitWindowSeconds;

    // 비밀번호 재설정 요청 제한 확인 메서드
    public void validateAllowed(String email) {
        Long currentCount = redisCounterSupport.incrementWithTtl(createRedisKey(email), getResetLimitWindow());

        if (currentCount == null || currentCount > resetLimitCount) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    // 요청 제한 Redis 키 생성 메서드
    private String createRedisKey(String email) {
        return RESET_LIMIT_KEY_PREFIX + ":" + email.toLowerCase();
    }

    // 비밀번호 재설정 요청 제한 시간 계산 메서드
    private Duration getResetLimitWindow() {
        return Duration.ofSeconds(resetLimitWindowSeconds);
    }
}
