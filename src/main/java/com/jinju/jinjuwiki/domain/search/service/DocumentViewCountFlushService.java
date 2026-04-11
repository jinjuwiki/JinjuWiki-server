package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 문서 조회수 Redis 누적값 DB 반영 서비스 클래스
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentViewCountFlushService {

    private static final String VIEW_COUNT_BUFFER_KEY = "document:view-count:pending";
    private static final String VIEW_COUNT_PROCESSING_KEY = "document:view-count:processing";
    private static final String VIEW_COUNT_FLUSH_LOCK_KEY = "document:view-count:flush-lock";
    private static final Duration VIEW_COUNT_FLUSH_LOCK_TTL = Duration.ofSeconds(55);

    private final StringRedisTemplate stringRedisTemplate;
    private final DocumentRepository documentRepository;

    // 문서 조회수 pending flush 메서드
    @Transactional
    public int flushPendingViewCounts() {
        if (!tryAcquireFlushLock()) {
            log.debug("문서 조회수 flush 스킵, flush lock 미획득");
            return 0;
        }

        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        try {
            Map<Object, Object> pendingEntries = drainPendingEntries(hashOperations);
            int flushedCount = 0;

            for (Map.Entry<Object, Object> pendingEntry : pendingEntries.entrySet()) {
                Long documentId = parseDocumentId(pendingEntry.getKey());
                long delta = parseViewCountDelta(pendingEntry.getValue());

                if (documentRepository.incrementViewCountBy(documentId, delta) > 0) {
                    hashOperations.delete(VIEW_COUNT_PROCESSING_KEY, pendingEntry.getKey().toString());
                    flushedCount++;
                } else {
                    log.warn("문서 조회수 flush 반영 실패, documentId={}, delta={}", documentId, delta);
                }
            }

            if (flushedCount > 0) {
                log.info("문서 조회수 flush 완료, flushedCount={}", flushedCount);
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

    // pending 해시를 processing 해시로 분리하는 메서드
    private Map<Object, Object> drainPendingEntries(HashOperations<String, Object, Object> hashOperations) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(VIEW_COUNT_PROCESSING_KEY))) {
            Map<Object, Object> processingEntries = hashOperations.entries(VIEW_COUNT_PROCESSING_KEY);
            if (!processingEntries.isEmpty()) {
                log.warn("문서 조회수 flush 재개, processingCount={}", processingEntries.size());
                return processingEntries;
            }

            stringRedisTemplate.delete(VIEW_COUNT_PROCESSING_KEY);
        }

        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(VIEW_COUNT_BUFFER_KEY))) {
            return Map.of();
        }

        stringRedisTemplate.rename(VIEW_COUNT_BUFFER_KEY, VIEW_COUNT_PROCESSING_KEY);
        Map<Object, Object> processingEntries = hashOperations.entries(VIEW_COUNT_PROCESSING_KEY);
        if (!processingEntries.isEmpty()) {
            log.debug("문서 조회수 flush drain 완료, processingCount={}", processingEntries.size());
        }
        return processingEntries;
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
