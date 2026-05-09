package com.jinju.jinjuwiki.domain.document.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "문서 상세 조회 응답")
public record DocumentDetailResponse(
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
        @Schema(description = "연결된 학교 문서 ID", example = "101", nullable = true)
        Long schoolDocumentId,
        @Schema(description = "연결된 학교 문서 제목", example = "진주고등학교", nullable = true)
        String schoolName,
        @Schema(description = "사건 발생 연도, 일반 문서는 null 가능", example = "1592", nullable = true)
        Integer eventYear,
        @Schema(description = "작성자 사용자 ID", example = "7")
        Long authorId,
        @Schema(description = "작성자 닉네임", example = "jinju-admin")
        String authorNickname,
        @Schema(
                description = "문서 본문 JSON",
                example = "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"진주성 전투 본문\"}]}]}"
        )
        JsonNode contentJson,
        @Schema(description = "문서 조회수", example = "128")
        Long viewCount,
        @Schema(description = "문서 생성 시각", example = "2026-04-14T10:30:00")
        LocalDateTime createdAt,
        @Schema(description = "문서 최종 수정 시각", example = "2026-04-14T11:45:00")
        LocalDateTime updatedAt
) {
}
