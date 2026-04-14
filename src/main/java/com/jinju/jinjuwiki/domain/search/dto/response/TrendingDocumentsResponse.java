package com.jinju.jinjuwiki.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

// 급상승 문서 목록 응답 DTO
@Schema(description = "급상승 문서 목록 응답")
public record TrendingDocumentsResponse(
        @Schema(description = "급상승 집계 기준 설명", example = "최근 1시간 동안 많이 조회된 문서")
        String description,
        @Schema(description = "급상승 문서 목록")
        List<TrendingDocumentItemResponse> documents
) {
}
