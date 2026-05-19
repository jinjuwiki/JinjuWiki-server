package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.config.RedisCounterSupport;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 이메일 인증 발송 요청 제한 Redis 클래스
@Component
@RequiredArgsConstructor
public class RedisEmailVerificationSendRateLimiter {

    private static final String SEND_LIMIT_KEY_PREFIX = "auth:email-send";

    private final RedisCounterSupport redisCounterSupport;

    @Value("${app.auth.email-verification-send-rate-limit.count:3}")
    private long sendLimitCount;

    @Value("${app.auth.email-verification-send-rate-limit.window-seconds:60}")
    private long sendLimitWindowSeconds;

    // 이메일 인증 발송 요청 제한 확인 메서드
    public void validateAllowed(String email) {
        Long currentCount = redisCounterSupport.incrementWithTtl(createRedisKey(email), getSendLimitWindow());

        if (currentCount == null || currentCount > sendLimitCount) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    // 요청 제한 Redis 키 생성 메서드
    private String createRedisKey(String email) {
        return SEND_LIMIT_KEY_PREFIX + ":" + email.toLowerCase();
    }

    // 이메일 인증 발송 제한 시간 계산 메서드
    private Duration getSendLimitWindow() {
        return Duration.ofSeconds(sendLimitWindowSeconds);
    }
}
