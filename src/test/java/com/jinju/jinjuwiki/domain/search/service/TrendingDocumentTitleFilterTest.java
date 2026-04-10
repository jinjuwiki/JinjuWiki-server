package com.jinju.jinjuwiki.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 급상승 문서 제목 필터 단위 테스트 클래스
class TrendingDocumentTitleFilterTest {

    private final TrendingDocumentTitleFilter trendingDocumentTitleFilter = new TrendingDocumentTitleFilter();

    @Test
    @DisplayName("제목에 개인정보가 포함되면 제외 대상으로 판단한다.")
    void containsPersonalInformationReturnsTrue() {
        // given
        String title = "문의는 test@example.com 또는 010-1234-5678";

        // when
        boolean result = trendingDocumentTitleFilter.containsPersonalInformation(title);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제목에 개인정보가 없으면 제외 대상으로 판단하지 않는다.")
    void containsPersonalInformationReturnsFalse() {
        // given
        String title = "진주고 축제 운영 정리";

        // when
        boolean result = trendingDocumentTitleFilter.containsPersonalInformation(title);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("제목에 금칙어가 포함되면 제외 대상으로 판단한다.")
    void containsBannedExpressionReturnsTrue() {
        // given
        String title = "저 사람은 사기꾼 맞음";

        // when
        boolean result = trendingDocumentTitleFilter.containsBannedExpression(title);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제목에 금칙어가 없으면 제외 대상으로 판단하지 않는다.")
    void containsBannedExpressionReturnsFalse() {
        // given
        String title = "진주고 동아리 발표회";

        // when
        boolean result = trendingDocumentTitleFilter.containsBannedExpression(title);

        // then
        assertThat(result).isFalse();
    }
}
