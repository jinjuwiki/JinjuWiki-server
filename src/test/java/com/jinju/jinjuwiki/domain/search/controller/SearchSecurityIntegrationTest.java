package com.jinju.jinjuwiki.domain.search.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.jinju.jinjuwiki.domain.search.dto.response.TrendingDocumentItemResponse;
import com.jinju.jinjuwiki.domain.search.dto.response.TrendingDocumentsResponse;
import com.jinju.jinjuwiki.domain.search.service.TrendingDocumentService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

// 급상승 문서 보안 통합 테스트 클래스
@SpringBootTest
@ActiveProfiles("integration")
class SearchSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private TrendingDocumentService trendingDocumentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("비로그인 사용자도 급상승 문서를 조회할 수 있다.")
    void getTrendingDocumentsWithoutAuthentication() throws Exception {
        // given
        TrendingDocumentsResponse response = new TrendingDocumentsResponse(
                "최근 1시간 동안 많이 조회된 문서를 보여줍니다.",
                List.of(new TrendingDocumentItemResponse(1L, "진주고 축제"))
        );
        when(trendingDocumentService.getTrendingDocuments()).thenReturn(response);

        // when
        var result = mockMvc.perform(get("/api/search/trending"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("최근 1시간 동안 많이 조회된 문서를 보여줍니다."))
                .andExpect(jsonPath("$.data.documents[0].documentId").value(1L))
                .andExpect(jsonPath("$.data.documents[0].title").value("진주고 축제"));
    }
}
