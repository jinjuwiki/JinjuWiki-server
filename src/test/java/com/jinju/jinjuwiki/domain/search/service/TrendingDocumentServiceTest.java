package com.jinju.jinjuwiki.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import com.jinju.jinjuwiki.domain.search.dto.response.TrendingDocumentsResponse;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

// 급상승 문서 서비스 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class TrendingDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private RedisTrendingDocumentViewBuffer redisTrendingDocumentViewBuffer;

    @Mock
    private TrendingDocumentTitleFilter trendingDocumentTitleFilter;

    @Mock
    private TrendingDocumentCandidatePolicy trendingDocumentCandidatePolicy;

    @InjectMocks
    private TrendingDocumentServiceImpl trendingDocumentService;

    @Test
    @DisplayName("급상승 문서는 집계 정렬 순서를 유지하고 상위 5개만 반환한다.")
    void getTrendingDocumentsSuccess() {
        // given
        List<Long> trendingDocumentIds = List.of(2L, 3L, 1L, 4L, 5L, 6L);
        List<Document> documents = List.of(
                createDocument(1L, "문서 1"),
                createDocument(2L, "문서 2"),
                createDocument(3L, "문서 3"),
                createDocument(4L, "문서 4"),
                createDocument(5L, "문서 5"),
                createDocument(6L, "문서 6")
        );
        when(redisTrendingDocumentViewBuffer.findTrendingDocumentIds()).thenReturn(trendingDocumentIds);
        when(documentRepository.findAllById(List.of(2L, 3L, 1L, 4L, 5L, 6L))).thenReturn(documents);
        when(trendingDocumentCandidatePolicy.matchesDocumentState(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(trendingDocumentTitleFilter.isExcludedTitle(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);

        // when
        TrendingDocumentsResponse response = trendingDocumentService.getTrendingDocuments();

        // then
        assertThat(response.description()).isEqualTo("최근 1시간 동안 많이 조회된 문서를 보여줍니다.");
        assertThat(response.documents()).hasSize(5);
        assertThat(response.documents().get(0).documentId()).isEqualTo(2L);
        assertThat(response.documents().get(1).documentId()).isEqualTo(3L);
        assertThat(response.documents().get(2).documentId()).isEqualTo(1L);
        assertThat(response.documents().get(3).documentId()).isEqualTo(4L);
        assertThat(response.documents().get(4).documentId()).isEqualTo(5L);
    }

    // 테스트용 문서 생성 메서드
    private Document createDocument(Long id, String title) {
        Category category = Category.builder()
                .name("학교")
                .build();
        ReflectionTestUtils.setField(category, "id", 1L);

        User author = User.builder()
                .email("author@test.com")
                .password("encoded-password")
                .nickname("author")
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(author, "id", 1L);

        Document document = Document.builder()
                .title(title)
                .content("{}")
                .summary("요약")
                .eventYear(2024)
                .contentJson("{}")
                .author(author)
                .category(category)
                .build();
        ReflectionTestUtils.setField(document, "id", id);
        return document;
    }
}
