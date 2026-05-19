package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.global.config.RedisCounterSupport;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 로그인 실패 제한 Redis 클래스
@Component
@RequiredArgsConstructor
public class RedisLoginAttemptLimiter {

    private static final String LOGIN_FAILURE_KEY_PREFIX = "auth:login-failure";

    private final RedisCounterSupport redisCounterSupport;

    @Value("${app.auth.login-attempt-limit.count:5}")
    private long loginFailureLimitCount;

    @Value("${app.auth.login-attempt-limit.window-seconds:300}")
    private long loginFailureWindowSeconds;

    // 로그인 가능 여부 확인 메서드
    public boolean isAllowed(String email) {
        String failureCount = redisCounterSupport.get(createRedisKey(email));
        if (failureCount == null) {
            return true;
        }

        return Long.parseLong(failureCount) < loginFailureLimitCount;
    }

    // 로그인 실패 누적 메서드
    public long recordFailure(String email) {
        Long currentCount = redisCounterSupport.incrementWithTtl(createRedisKey(email), getLoginFailureWindow());

        if (currentCount == null) {
            throw new IllegalStateException("로그인 실패 횟수 누적에 실패했습니다.");
        }

        return currentCount;
    }

    // 로그인 실패 초기화 메서드
    public void reset(String email) {
        redisCounterSupport.delete(createRedisKey(email));
    }

    private String createRedisKey(String email) {
        return LOGIN_FAILURE_KEY_PREFIX + ":" + email.toLowerCase();
    }

    private Duration getLoginFailureWindow() {
        return Duration.ofSeconds(loginFailureWindowSeconds);
    }

    // 로그인 실패 제한 도달 여부 확인 메서드
    public boolean hasReachedLimit(long failureCount) {
        return failureCount >= loginFailureLimitCount;
    }
}
