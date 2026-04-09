package com.jinju.jinjuwiki.domain.user.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jinju.jinjuwiki.domain.user.dto.response.UserProfileResponse;
import com.jinju.jinjuwiki.domain.user.service.UserService;
import com.jinju.jinjuwiki.global.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

// 사용자 컨트롤러 MockMvc 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilters(new TestAuthenticationFilter())
                .build();
    }

    @Test
    @DisplayName("로그인한 사용자는 자신의 프로필을 조회할 수 있다.")
    void getMyProfileSuccess() throws Exception {
        // given
        UserProfileResponse response = new UserProfileResponse(
                1L,
                "profile@test.com",
                "profileUser",
                "USER",
                LocalDateTime.of(2026, 4, 9, 13, 0),
                LocalDateTime.of(2026, 4, 9, 13, 5)
        );
        when(userService.getProfile(1L)).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer test-token"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.email").value("profile@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("profileUser"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());

        verify(userService).getProfile(1L);
    }

    @Test
    @DisplayName("인증 없이 프로필을 조회하면 401을 반환한다.")
    void getMyProfileUnauthorized() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/users/me"));

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

            UserPrincipal principal = new UserPrincipal(1L, "profile@test.com", "encoded-password", "USER");
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
