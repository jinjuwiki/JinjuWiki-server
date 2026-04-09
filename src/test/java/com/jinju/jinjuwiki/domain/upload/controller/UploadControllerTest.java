package com.jinju.jinjuwiki.domain.upload.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jinju.jinjuwiki.domain.upload.dto.response.ImageUploadResponse;
import com.jinju.jinjuwiki.domain.upload.service.UploadService;
import com.jinju.jinjuwiki.global.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

// 업로드 컨트롤러 MockMvc 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class UploadControllerTest {

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private UploadController uploadController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(uploadController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilters(new TestAuthenticationFilter())
                .build();
    }

    @Test
    @DisplayName("로그인한 사용자는 이미지를 업로드할 수 있다.")
    void uploadImageSuccess() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "sample.png",
                "image/png",
                "image-content".getBytes()
        );
        ImageUploadResponse response = new ImageUploadResponse("/uploads/images/sample.png");
        when(uploadService.uploadImage(any())).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(
                multipart("/api/uploads/images")
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("이미지가 업로드되었습니다."))
                .andExpect(jsonPath("$.data.imageUrl").value("/uploads/images/sample.png"));

        verify(uploadService).uploadImage(any());
    }

    @Test
    @DisplayName("인증 없이 이미지를 업로드하면 401을 반환한다.")
    void uploadImageUnauthorized() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "sample.png",
                "image/png",
                "image-content".getBytes()
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/uploads/images").file(image));

        // then
        result.andExpect(status().isUnauthorized());
    }

    // 테스트용 인증 필터 클래스
    private static final class TestAuthenticationFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            UserPrincipal principal = new UserPrincipal(1L, "upload@test.com", "encoded-password", "USER");
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            try {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }
}
