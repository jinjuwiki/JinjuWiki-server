package com.jinju.jinjuwiki.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

// 문서 조회수 Redis 버퍼 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class RedisDocumentViewCountBufferTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private RedisDocumentViewCountBuffer redisDocumentViewCountBuffer;

    @Test
    @DisplayName("문서 조회수 버퍼는 Redis 해시 필드를 1 증가시킨다.")
    void incrementSuccess() {
        // given
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.increment("document:view-count:pending", "10", 1L)).thenReturn(3L);

        // when
        long updatedCount = redisDocumentViewCountBuffer.increment(10L);

        // then
        assertThat(updatedCount).isEqualTo(3L);
        verify(hashOperations).increment("document:view-count:pending", "10", 1L);
    }

    @Test
    @DisplayName("문서 조회수 버퍼 증가 결과가 없으면 예외를 발생시킨다.")
    void incrementFailWhenRedisReturnsNull() {
        // given
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.increment("document:view-count:pending", "10", 1L)).thenReturn(null);

        // when
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> redisDocumentViewCountBuffer.increment(10L)
        );

        // then
        assertThat(exception.getMessage()).isEqualTo("문서 조회수 버퍼 증가 실패");
    }
}
