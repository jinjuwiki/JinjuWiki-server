package com.jinju.jinjuwiki.domain.document.controller;

import com.jinju.jinjuwiki.domain.document.dto.request.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentUpdateRequest;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.mapper.DocumentCreateResponseMapper;
import com.jinju.jinjuwiki.domain.document.mapper.DocumentDetailResponseMapper;
import com.jinju.jinjuwiki.domain.document.mapper.DocumentSummaryResponseMapper;
import com.jinju.jinjuwiki.domain.document.service.DocumentService;
import com.jinju.jinjuwiki.global.response.ApiResponse;
import com.jinju.jinjuwiki.global.response.PageResponse;
import com.jinju.jinjuwiki.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

// 문서 CRUD 처리 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
@Tag(name = "Document", description = "문서 CRUD 및 검색 API")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @Operation(
            summary = "문서 작성",
            description = "새 문서를 작성합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<DocumentCreateResponse>> createDocument(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DocumentCreateRequest request
    ) {
        Document document = documentService.createDocument(request, userPrincipal.getId());
        DocumentCreateResponse response = DocumentCreateResponseMapper.toResponse(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("문서가 생성되었습니다.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "문서 상세 조회", description = "문서 상세 정보와 내용을 조회합니다.")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request
    ) {
        Long viewerUserId = userPrincipal == null ? null : userPrincipal.getId();
        Document document = documentService.getDocument(id, viewerUserId, request.getRemoteAddr());
        DocumentDetailResponse response = DocumentDetailResponseMapper.toResponse(document);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping
    @Operation(summary = "문서 목록 조회", description = "카테고리별 문서 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<DocumentSummaryResponse>>> getDocuments(
            @RequestParam(required = false) Long categoryId, // 카테고리 필터
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Document> documents = documentService.getDocuments(categoryId, page, size);
        PageResponse<DocumentSummaryResponse> response =
                PageResponse.from(documents.map(DocumentSummaryResponseMapper::toResponse));
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping("/search")
    @Operation(summary = "문서 검색", description = "키워드와 카테고리 조건으로 문서를 검색합니다.")
    public ResponseEntity<ApiResponse<PageResponse<DocumentSummaryResponse>>> searchDocuments(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Document> documents = documentService.searchDocuments(keyword, categoryId, page, size);
        PageResponse<DocumentSummaryResponse> response =
                PageResponse.from(documents.map(DocumentSummaryResponseMapper::toResponse));
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "문서 수정",
            description = "작성자 본인의 문서를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> updateDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DocumentUpdateRequest request
    ) {
        Document document = documentService.updateDocument(id, request, userPrincipal.getId());
        DocumentDetailResponse response = DocumentDetailResponseMapper.toResponse(document);
        return ResponseEntity.ok(ApiResponse.of("문서가 수정되었습니다.", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "문서 삭제",
            description = "작성자 본인의 문서를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        documentService.deleteDocument(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("문서가 삭제되었습니다."));
    }
}
