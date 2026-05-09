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

// 로그인 실패 제한 Redis 클래스
@Component
@RequiredArgsConstructor
public class RedisLoginAttemptLimiter {

    private static final String LOGIN_FAILURE_KEY_PREFIX = "auth:login-failure";
    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL_SCRIPT = createIncrementWithTtlScript();

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.auth.login-attempt-limit.count:5}")
    private long loginFailureLimitCount;

    @Value("${app.auth.login-attempt-limit.window-seconds:300}")
    private long loginFailureWindowSeconds;

    // 로그인 가능 여부 확인 메서드
    public void validateAllowed(String email) {
        String failureCount = stringRedisTemplate.opsForValue().get(createRedisKey(email));
        if (failureCount == null) {
            return;
        }

        if (Long.parseLong(failureCount) >= loginFailureLimitCount) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    // 로그인 실패 누적 메서드
    public long recordFailure(String email) {
        Long currentCount = stringRedisTemplate.execute(
                INCREMENT_WITH_TTL_SCRIPT,
                List.of(createRedisKey(email)),
                String.valueOf(getLoginFailureWindow().toSeconds())
        );

        if (currentCount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return currentCount;
    }

    // 로그인 실패 초기화 메서드
    public void reset(String email) {
        stringRedisTemplate.delete(createRedisKey(email));
    }

    private String createRedisKey(String email) {
        return LOGIN_FAILURE_KEY_PREFIX + ":" + email.toLowerCase();
    }

    private Duration getLoginFailureWindow() {
        return Duration.ofSeconds(loginFailureWindowSeconds);
    }

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
