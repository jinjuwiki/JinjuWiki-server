package com.jinju.jinjuwiki.domain.search.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// 문서 조회수 flush 스케줄러 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class DocumentViewCountFlushSchedulerTest {

    @Mock
    private DocumentViewCountFlushService documentViewCountFlushService;

    @InjectMocks
    private DocumentViewCountFlushScheduler documentViewCountFlushScheduler;

    @Test
    @DisplayName("문서 조회수 flush 스케줄러는 flush 서비스를 호출한다.")
    void flushPendingViewCountsCallsFlushService() {
        // when
        documentViewCountFlushScheduler.flushPendingViewCounts();

        // then
        verify(documentViewCountFlushService).flushPendingViewCounts();
    }
}
