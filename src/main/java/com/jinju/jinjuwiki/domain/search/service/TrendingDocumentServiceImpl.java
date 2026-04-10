package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import com.jinju.jinjuwiki.domain.search.dto.response.TrendingDocumentItemResponse;
import com.jinju.jinjuwiki.domain.search.dto.response.TrendingDocumentsResponse;
import com.jinju.jinjuwiki.domain.search.repository.DocumentViewLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 급상승 문서 조회 서비스 구현체
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendingDocumentServiceImpl implements TrendingDocumentService {

    private static final int TRENDING_DOCUMENT_LIMIT = 5;
    private static final String TRENDING_DESCRIPTION = "최근 1시간 동안 많이 조회된 문서를 보여줍니다.";

    private final DocumentViewLogRepository documentViewLogRepository;
    private final DocumentRepository documentRepository;
    private final TrendingDocumentTitleFilter trendingDocumentTitleFilter;
    private final TrendingDocumentCandidatePolicy trendingDocumentCandidatePolicy;

    @Override
    public TrendingDocumentsResponse getTrendingDocuments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime threeHoursAgo = now.minusHours(3);

        List<DocumentViewLogRepository.TrendingDocumentProjection> projections =
                documentViewLogRepository.findTrendingDocumentsSince(oneHourAgo, threeHoursAgo);
        Map<Long, Document> documentMap = findDocuments(projections);

        List<TrendingDocumentItemResponse> documents = projections.stream()
                .map(projection -> documentMap.get(projection.getDocumentId()))
                .filter(document -> document != null)
                .filter(trendingDocumentCandidatePolicy::matchesDocumentState)
                .filter(document -> !trendingDocumentTitleFilter.containsPersonalInformation(document.getTitle()))
                .filter(document -> !trendingDocumentTitleFilter.containsBannedExpression(document.getTitle()))
                .limit(TRENDING_DOCUMENT_LIMIT)
                .map(document -> new TrendingDocumentItemResponse(document.getId(), document.getTitle()))
                .toList();

        return new TrendingDocumentsResponse(TRENDING_DESCRIPTION, documents);
    }

    // 집계 대상 문서 조회 메서드
    private Map<Long, Document> findDocuments(List<DocumentViewLogRepository.TrendingDocumentProjection> projections) {
        List<Long> documentIds = projections.stream()
                .map(DocumentViewLogRepository.TrendingDocumentProjection::getDocumentId)
                .toList();

        return documentRepository.findAllById(documentIds).stream()
                .collect(java.util.stream.Collectors.toMap(Document::getId, Function.identity()));
    }
}
