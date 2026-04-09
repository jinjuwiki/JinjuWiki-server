package com.jinju.jinjuwiki.domain.document.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentCreateRequest(
        @NotBlank(message = "제목은 비어 있을 수 없습니다.")
        @Size(max = 150, message = "제목은 150자 이하로 입력해야 합니다.")
        String title,

        @NotBlank(message = "요약은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "요약은 255자 이하로 입력해야 합니다.")
        String summary,

        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        Integer eventYear,

        @NotNull(message = "본문 JSON은 비어 있을 수 없습니다.")
        JsonNode contentJson
) {
}
