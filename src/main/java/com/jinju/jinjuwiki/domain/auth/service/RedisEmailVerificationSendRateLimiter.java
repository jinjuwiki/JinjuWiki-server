package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

// 이메일 인증 발송 요청 제한 Redis 클래스
@Component
@RequiredArgsConstructor
public class RedisEmailVerificationSendRateLimiter {

    private static final String SEND_LIMIT_KEY_PREFIX = "auth:email-send";
    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL_SCRIPT = createIncrementWithTtlScript();

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.auth.email-verification-send-rate-limit.count:3}")
    private long sendLimitCount;

    @Value("${app.auth.email-verification-send-rate-limit.window-seconds:60}")
    private long sendLimitWindowSeconds;

    // 이메일 인증 발송 요청 제한 확인 메서드
    public void validateAllowed(String email) {
        Long currentCount = stringRedisTemplate.execute(
                INCREMENT_WITH_TTL_SCRIPT,
                List.of(createRedisKey(email)),
                String.valueOf(getSendLimitWindow().toSeconds())
        );

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

    // TTL 포함 증가 Lua 스크립트 생성 메서드
    private static DefaultRedisScript<Long> createIncrementWithTtlScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText("""
                local current = redis.call('INCR', KEYS[1])
                if current == 1 then
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                end
                return current
                """);
        return script;
    }
}
