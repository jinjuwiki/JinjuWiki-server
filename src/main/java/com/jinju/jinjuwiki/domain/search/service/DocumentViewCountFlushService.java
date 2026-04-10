package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 문서 조회수 Redis 누적값 DB 반영 서비스 클래스
@Service
@RequiredArgsConstructor
public class DocumentViewCountFlushService {

    private static final String VIEW_COUNT_BUFFER_KEY = "document:view-count:pending";
    private static final String VIEW_COUNT_FLUSH_LOCK_KEY = "document:view-count:flush-lock";
    private static final Duration VIEW_COUNT_FLUSH_LOCK_TTL = Duration.ofSeconds(55);

    private final StringRedisTemplate stringRedisTemplate;
    private final DocumentRepository documentRepository;

    // 문서 조회수 pending flush 메서드
    @Transactional
    public int flushPendingViewCounts() {
        if (!tryAcquireFlushLock()) {
            return 0;
        }

        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        try {
            Map<Object, Object> pendingEntries = hashOperations.entries(VIEW_COUNT_BUFFER_KEY);
            int flushedCount = 0;

            for (Map.Entry<Object, Object> pendingEntry : pendingEntries.entrySet()) {
                Long documentId = parseDocumentId(pendingEntry.getKey());
                long delta = parseViewCountDelta(pendingEntry.getValue());

                if (documentRepository.incrementViewCountBy(documentId, delta) > 0) {
                    hashOperations.delete(VIEW_COUNT_BUFFER_KEY, pendingEntry.getKey().toString());
                    flushedCount++;
                }
            }

            return flushedCount;
        } finally {
            releaseFlushLock();
        }
    }

    // Redis 해시 key 문서 ID 변환 메서드
    private Long parseDocumentId(Object rawDocumentId) {
        return Long.parseLong(rawDocumentId.toString());
    }

    // Redis 해시 value 조회수 delta 변환 메서드
    private long parseViewCountDelta(Object rawDelta) {
        return Long.parseLong(rawDelta.toString());
    }

    // flush 분산 락 획득 메서드
    private boolean tryAcquireFlushLock() {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        Boolean acquired = valueOperations.setIfAbsent(
                VIEW_COUNT_FLUSH_LOCK_KEY,
                "locked",
                VIEW_COUNT_FLUSH_LOCK_TTL
        );
        return Boolean.TRUE.equals(acquired);
    }

    // flush 분산 락 해제 메서드
    private void releaseFlushLock() {
        stringRedisTemplate.delete(VIEW_COUNT_FLUSH_LOCK_KEY);
    }
}
