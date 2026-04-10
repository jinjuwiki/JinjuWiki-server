package com.jinju.jinjuwiki.domain.search.dto.response;

// 급상승 문서 항목 응답 DTO
public record TrendingDocumentItemResponse(
        Long documentId,
        String title
) {
}
