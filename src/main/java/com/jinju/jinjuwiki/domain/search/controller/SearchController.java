package com.jinju.jinjuwiki.domain.search.controller;

import com.jinju.jinjuwiki.domain.search.dto.response.TrendingDocumentsResponse;
import com.jinju.jinjuwiki.domain.search.service.TrendingDocumentService;
import com.jinju.jinjuwiki.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 검색 관련 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Tag(name = "Search", description = "검색 및 급상승 문서 API")
public class SearchController {

    private final TrendingDocumentService trendingDocumentService;

    @GetMapping("/trending")
    @Operation(summary = "인기 급상승 문서 조회", description = "최근 1시간 동안 많이 조회된 문서를 조회합니다.")
    public ResponseEntity<ApiResponse<TrendingDocumentsResponse>> getTrendingDocuments() {
        TrendingDocumentsResponse response = trendingDocumentService.getTrendingDocuments();
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
