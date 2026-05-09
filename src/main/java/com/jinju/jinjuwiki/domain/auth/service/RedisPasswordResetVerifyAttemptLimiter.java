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

// 비밀번호 재설정 인증코드 확인 실패 제한 Redis 클래스
@Component
@RequiredArgsConstructor
public class RedisPasswordResetVerifyAttemptLimiter {

    private static final String PASSWORD_RESET_VERIFY_FAILURE_KEY_PREFIX = "auth:password-reset-verify-failure";
    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL_SCRIPT = createIncrementWithTtlScript();

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.auth.password-reset-verify-attempt-limit.count:5}")
    private long verifyFailureLimitCount;

    @Value("${app.auth.password-reset-verify-attempt-limit.window-seconds:300}")
    private long verifyFailureWindowSeconds;

    // 비밀번호 재설정 인증코드 확인 가능 여부 메서드
    public void validateAllowed(String email) {
        String failureCount = stringRedisTemplate.opsForValue().get(createRedisKey(email));
        if (failureCount == null) {
            return;
        }

        if (Long.parseLong(failureCount) >= verifyFailureLimitCount) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    // 비밀번호 재설정 인증코드 확인 실패 누적 메서드
    public long recordFailure(String email) {
        Long currentCount = stringRedisTemplate.execute(
                INCREMENT_WITH_TTL_SCRIPT,
                List.of(createRedisKey(email)),
                String.valueOf(getVerifyFailureWindow().toSeconds())
        );

        if (currentCount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return currentCount;
    }

    // 비밀번호 재설정 인증코드 확인 실패 초기화 메서드
    public void reset(String email) {
        stringRedisTemplate.delete(createRedisKey(email));
    }

    private String createRedisKey(String email) {
        return PASSWORD_RESET_VERIFY_FAILURE_KEY_PREFIX + ":" + email.toLowerCase();
    }

    private Duration getVerifyFailureWindow() {
        return Duration.ofSeconds(verifyFailureWindowSeconds);
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
