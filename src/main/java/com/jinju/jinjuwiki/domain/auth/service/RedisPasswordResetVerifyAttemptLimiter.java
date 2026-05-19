package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.global.config.RedisCounterSupport;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 비밀번호 재설정 인증코드 확인 실패 제한 Redis 클래스
@Component
@RequiredArgsConstructor
public class RedisPasswordResetVerifyAttemptLimiter {

    private static final String PASSWORD_RESET_VERIFY_FAILURE_KEY_PREFIX = "auth:password-reset-verify-failure";

    private final RedisCounterSupport redisCounterSupport;

    @Value("${app.auth.password-reset-verify-attempt-limit.count:5}")
    private long verifyFailureLimitCount;

    @Value("${app.auth.password-reset-verify-attempt-limit.window-seconds:300}")
    private long verifyFailureWindowSeconds;

    // 비밀번호 재설정 인증코드 확인 가능 여부 메서드
    public boolean isAllowed(String email) {
        String failureCount = redisCounterSupport.get(createRedisKey(email));
        if (failureCount == null) {
            return true;
        }

        return Long.parseLong(failureCount) < verifyFailureLimitCount;
    }

    // 비밀번호 재설정 인증코드 확인 실패 누적 메서드
    public long recordFailure(String email) {
        Long currentCount = redisCounterSupport.incrementWithTtl(createRedisKey(email), getVerifyFailureWindow());

        if (currentCount == null) {
            throw new IllegalStateException("비밀번호 재설정 인증코드 실패 횟수 누적에 실패했습니다.");
        }

        return currentCount;
    }

    // 비밀번호 재설정 인증코드 확인 실패 초기화 메서드
    public void reset(String email) {
        redisCounterSupport.delete(createRedisKey(email));
    }

    private String createRedisKey(String email) {
        return PASSWORD_RESET_VERIFY_FAILURE_KEY_PREFIX + ":" + email.toLowerCase();
    }

    private Duration getVerifyFailureWindow() {
        return Duration.ofSeconds(verifyFailureWindowSeconds);
    }

    // 비밀번호 재설정 인증코드 확인 실패 제한 도달 여부 확인 메서드
    public boolean hasReachedLimit(long failureCount) {
        return failureCount >= verifyFailureLimitCount;
    }
}
