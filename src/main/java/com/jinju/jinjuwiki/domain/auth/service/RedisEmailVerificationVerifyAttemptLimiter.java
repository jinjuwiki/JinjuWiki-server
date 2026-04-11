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

// 이메일 인증코드 검증 실패 제한 Redis 클래스
@Component
@RequiredArgsConstructor
public class RedisEmailVerificationVerifyAttemptLimiter {

    private static final String VERIFY_FAILURE_KEY_PREFIX = "auth:email-verify-failure";
    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL_SCRIPT = createIncrementWithTtlScript();

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.auth.email-verification-verify-attempt-limit.count:5}")
    private long verifyFailureLimitCount;

    @Value("${app.auth.email-verification-verify-attempt-limit.window-seconds:300}")
    private long verifyFailureWindowSeconds;

    // 이메일 인증코드 검증 가능 여부 확인 메서드
    public void validateAllowed(String email) {
        String failureCount = stringRedisTemplate.opsForValue().get(createRedisKey(email));
        if (failureCount == null) {
            return;
        }

        if (Long.parseLong(failureCount) >= verifyFailureLimitCount) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    // 이메일 인증코드 검증 실패 누적 메서드
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

    // 이메일 인증코드 검증 실패 초기화 메서드
    public void reset(String email) {
        stringRedisTemplate.delete(createRedisKey(email));
    }

    // 검증 실패 Redis 키 생성 메서드
    private String createRedisKey(String email) {
        return VERIFY_FAILURE_KEY_PREFIX + ":" + email.toLowerCase();
    }

    // 이메일 인증코드 검증 실패 제한 시간 계산 메서드
    private Duration getVerifyFailureWindow() {
        return Duration.ofSeconds(verifyFailureWindowSeconds);
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
