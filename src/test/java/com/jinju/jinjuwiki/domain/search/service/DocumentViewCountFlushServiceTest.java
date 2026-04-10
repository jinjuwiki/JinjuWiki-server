package com.jinju.jinjuwiki.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

// 문서 조회수 flush 서비스 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class DocumentViewCountFlushServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private DocumentViewCountFlushService documentViewCountFlushService;

    @Test
    @DisplayName("pending 조회수는 문서별 delta 만큼 DB에 반영 후 Redis 해시에서 제거한다.")
    void flushPendingViewCountsSuccess() {
        // flush 대상 Redis 해시 준비 메서드
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(valueOperations.setIfAbsent("document:view-count:flush-lock", "locked", Duration.ofSeconds(55)))
                .thenReturn(true);
        when(hashOperations.entries("document:view-count:pending")).thenReturn(Map.of("10", "3", "11", "2"));
        when(documentRepository.incrementViewCountBy(10L, 3L)).thenReturn(1);
        when(documentRepository.incrementViewCountBy(11L, 2L)).thenReturn(1);

        // when
        int flushedCount = documentViewCountFlushService.flushPendingViewCounts();

        // then
        assertThat(flushedCount).isEqualTo(2);
        verify(documentRepository).incrementViewCountBy(10L, 3L);
        verify(documentRepository).incrementViewCountBy(11L, 2L);
        verify(hashOperations).delete("document:view-count:pending", "10");
        verify(hashOperations).delete("document:view-count:pending", "11");
        verify(stringRedisTemplate).delete("document:view-count:flush-lock");
    }

    @Test
    @DisplayName("DB 반영 대상이 없으면 Redis pending 데이터는 삭제하지 않는다.")
    void flushPendingViewCountsSkipDeleteWhenUpdateTargetMissing() {
        // flush 대상 Redis 해시 준비 메서드
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(valueOperations.setIfAbsent("document:view-count:flush-lock", "locked", Duration.ofSeconds(55)))
                .thenReturn(true);
        when(hashOperations.entries("document:view-count:pending")).thenReturn(Map.of("10", "3"));
        when(documentRepository.incrementViewCountBy(10L, 3L)).thenReturn(0);

        // when
        int flushedCount = documentViewCountFlushService.flushPendingViewCounts();

        // then
        assertThat(flushedCount).isZero();
        verify(documentRepository).incrementViewCountBy(10L, 3L);
        verify(hashOperations, never()).delete("document:view-count:pending", "10");
        verify(stringRedisTemplate).delete("document:view-count:flush-lock");
    }

    @Test
    @DisplayName("flush 락을 획득하지 못하면 DB 반영 없이 즉시 종료한다.")
    void flushPendingViewCountsSkipWhenLockNotAcquired() {
        // flush 락 준비 메서드
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("document:view-count:flush-lock", "locked", Duration.ofSeconds(55)))
                .thenReturn(false);

        // when
        int flushedCount = documentViewCountFlushService.flushPendingViewCounts();

        // then
        assertThat(flushedCount).isZero();
        verify(documentRepository, never()).incrementViewCountBy(10L, 3L);
        verify(stringRedisTemplate, never()).opsForHash();
        verify(stringRedisTemplate, never()).delete("document:view-count:flush-lock");
    }
}
