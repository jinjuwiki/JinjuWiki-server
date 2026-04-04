package com.jinju.jinjuwiki.domain.category.controller;

import com.jinju.jinjuwiki.domain.category.dto.CategoryResponse;
import com.jinju.jinjuwiki.domain.category.service.CategoryService;
import com.jinju.jinjuwiki.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.of(categoryService.getCategories()));
    }
}
