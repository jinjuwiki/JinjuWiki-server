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
    @Operation(
            summary = "카테고리 목록 조회",
            description = "학교 카테고리 문서를 상위 노드로 사용하는 카테고리 트리를 조회합니다. 학교 문서 아래에는 학생, 선생님, 사건사고 하위 카테고리가 반복되며 기타 카테고리는 최상위에 그대로 노출됩니다."
    )
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
