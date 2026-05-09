package com.jinju.jinjuwiki.domain.category.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "카테고리 응답")
public record CategoryResponse(
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,
        @Schema(description = "학교 문서 ID", example = "101", nullable = true)
        Long schoolDocumentId,
        @Schema(description = "카테고리 이름", example = "역사")
        String name,
        @Schema(description = "하위 카테고리 목록")
        List<CategoryResponse> children
) {

    public static CategoryResponse leaf(Long categoryId, Long schoolDocumentId, String name) {
        return new CategoryResponse(categoryId, schoolDocumentId, name, List.of());
    }

    public static CategoryResponse parent(Long categoryId, Long schoolDocumentId, String name, List<CategoryResponse> children) {
        return new CategoryResponse(categoryId, schoolDocumentId, name, children);
    }
}
