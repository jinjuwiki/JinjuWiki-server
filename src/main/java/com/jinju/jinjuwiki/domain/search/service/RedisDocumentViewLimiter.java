package com.jinju.jinjuwiki.domain.search.service;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

// Redis 기반 문서 조회 제한기 클래스
@Component
@RequiredArgsConstructor
public class RedisDocumentViewLimiter {

    private static final long VIEW_LIMIT_PER_HOUR = 3L;
    private static final Duration VIEW_WINDOW = Duration.ofHours(1);
    private static final String VIEW_LIMIT_KEY_PREFIX = "document:view-limit";
    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL_SCRIPT = createIncrementWithTtlScript();

    private final StringRedisTemplate stringRedisTemplate;

    // 조회 허용 여부 확인 메서드
    public boolean isAllowed(Long documentId, Long viewerUserId, String viewerIp) {
        String redisKey = createRedisKey(documentId, viewerUserId, viewerIp);
        Long currentCount = stringRedisTemplate.execute(
                INCREMENT_WITH_TTL_SCRIPT,
                List.of(redisKey),
                String.valueOf(VIEW_WINDOW.toSeconds())
        );

        return currentCount != null && currentCount <= VIEW_LIMIT_PER_HOUR;
    }

    // 조회 제한 Redis 키 생성 메서드
    private String createRedisKey(Long documentId, Long viewerUserId, String viewerIp) {
        if (viewerUserId != null) {
            return VIEW_LIMIT_KEY_PREFIX + ":document:" + documentId + ":user:" + viewerUserId;
        }

        return VIEW_LIMIT_KEY_PREFIX + ":document:" + documentId + ":ip:" + viewerIp;
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
