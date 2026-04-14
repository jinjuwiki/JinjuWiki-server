package com.jinju.jinjuwiki.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

// 급상승 문서 항목 응답 DTO
@Schema(description = "급상승 문서 항목 응답")
public record TrendingDocumentItemResponse(
        @Schema(description = "문서 ID", example = "42")
        Long documentId,
        @Schema(description = "문서 제목", example = "진주성 전투")
        String title
) {
}
