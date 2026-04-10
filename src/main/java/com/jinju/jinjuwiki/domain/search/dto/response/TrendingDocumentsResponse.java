package com.jinju.jinjuwiki.domain.search.dto.response;

import java.util.List;

// 급상승 문서 목록 응답 DTO
public record TrendingDocumentsResponse(
        String description,
        List<TrendingDocumentItemResponse> documents
) {
}
