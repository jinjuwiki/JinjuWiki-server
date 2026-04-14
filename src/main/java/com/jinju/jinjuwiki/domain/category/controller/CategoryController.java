package com.jinju.jinjuwiki.domain.category.controller;

import com.jinju.jinjuwiki.domain.category.dto.response.CategoryResponse;
import com.jinju.jinjuwiki.domain.category.service.CategoryService;
import com.jinju.jinjuwiki.global.error.ErrorResponse;
import com.jinju.jinjuwiki.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 카테고리 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "Category", description = "카테고리 조회 API")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "문서 작성과 탐색에 사용하는 카테고리 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "카테고리 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "카테고리 목록 조회 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 카테고리 목록 조회 응답 코드 명세
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> response = categoryService.getCategories();
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
