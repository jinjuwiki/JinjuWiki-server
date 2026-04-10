package com.jinju.jinjuwiki.domain.search.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jinju.jinjuwiki.domain.search.dto.response.TrendingDocumentItemResponse;
import com.jinju.jinjuwiki.domain.search.dto.response.TrendingDocumentsResponse;
import com.jinju.jinjuwiki.domain.search.service.TrendingDocumentService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// 검색 컨트롤러 MockMvc 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private TrendingDocumentService trendingDocumentService;

    @InjectMocks
    private SearchController searchController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchController).build();
    }

    @Test
    @DisplayName("급상승 문서 목록을 조회할 수 있다.")
    void getTrendingDocumentsSuccess() throws Exception {
        // given
        TrendingDocumentsResponse response = new TrendingDocumentsResponse(
                "최근 1시간 동안 많이 조회된 문서를 보여줍니다.",
                List.of(
                        new TrendingDocumentItemResponse(1L, "진주고 축제"),
                        new TrendingDocumentItemResponse(2L, "진주여고 체육대회")
                )
        );
        when(trendingDocumentService.getTrendingDocuments()).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/search/trending"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data.description").value("최근 1시간 동안 많이 조회된 문서를 보여줍니다."))
                .andExpect(jsonPath("$.data.documents[0].documentId").value(1L))
                .andExpect(jsonPath("$.data.documents[0].title").value("진주고 축제"))
                .andExpect(jsonPath("$.data.documents[1].documentId").value(2L))
                .andExpect(jsonPath("$.data.documents[1].title").value("진주여고 체육대회"));

        verify(trendingDocumentService).getTrendingDocuments();
    }
}
