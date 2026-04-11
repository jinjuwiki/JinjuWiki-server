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

// 비밀번호 재설정 요청 제한 Redis 클래스
@Component
@RequiredArgsConstructor
public class RedisPasswordResetRequestRateLimiter {

    private static final String RESET_LIMIT_KEY_PREFIX = "auth:password-reset";
    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL_SCRIPT = createIncrementWithTtlScript();

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.auth.password-reset-request-rate-limit.count:5}")
    private long resetLimitCount;

    @Value("${app.auth.password-reset-request-rate-limit.window-seconds:300}")
    private long resetLimitWindowSeconds;

    // 비밀번호 재설정 요청 제한 확인 메서드
    public void validateAllowed(String email) {
        Long currentCount = stringRedisTemplate.execute(
                INCREMENT_WITH_TTL_SCRIPT,
                List.of(createRedisKey(email)),
                String.valueOf(getResetLimitWindow().toSeconds())
        );

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
