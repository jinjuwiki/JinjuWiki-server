package com.jinju.jinjuwiki.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "페이지네이션 응답")
public record PageResponse<T>(
        @Schema(description = "현재 페이지의 데이터 목록")
        List<T> content,
        @Schema(description = "현재 페이지 번호, 0부터 시작", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "10")
        int size,
        @Schema(description = "전체 데이터 개수", example = "125")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "13")
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
