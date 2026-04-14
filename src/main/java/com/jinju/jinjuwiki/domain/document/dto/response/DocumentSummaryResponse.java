package com.jinju.jinjuwiki.domain.document.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "문서 목록 요약 응답")
public record DocumentSummaryResponse(
        @Schema(description = "문서 ID", example = "42")
        Long documentId,
        @Schema(description = "문서 제목", example = "진주성 전투")
        String title,
        @Schema(description = "문서 요약", example = "임진왜란 당시 진주성에서 벌어진 전투를 정리한 문서")
        String summary,
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,
        @Schema(description = "카테고리 이름", example = "역사")
        String categoryName,
        @Schema(description = "사건 발생 연도, 일반 문서는 null 가능", example = "1592", nullable = true)
        Integer eventYear,
        @Schema(description = "작성자 닉네임", example = "jinju-admin")
        String authorNickname,
        @Schema(description = "문서 조회수", example = "128")
        Long viewCount,
        @Schema(description = "문서 생성 시각", example = "2026-04-14T10:30:00")
        LocalDateTime createdAt
) {
}
