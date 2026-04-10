package com.jinju.jinjuwiki.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 필터 제외 대상 경로 패턴
    private static final PathPatternParser PATH_PATTERN_PARSER = new PathPatternParser();
    private static final List<PathPattern> EXCLUDED_PATH_PATTERNS = List.of(
            PATH_PATTERN_PARSER.parse("/swagger-ui.html"),
            PATH_PATTERN_PARSER.parse("/swagger-ui/**"),
            PATH_PATTERN_PARSER.parse("/v3/api-docs/**"),
            PATH_PATTERN_PARSER.parse("/error")
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        if (EXCLUDED_PATH_PATTERNS.stream().anyMatch(pattern -> pattern.matches(PathContainerParser.from(requestUri)))) {
            return true;
        }

        if (HttpMethod.POST.matches(method) && isPublicAuthPath(requestUri)) {
            return true;
        }

        return HttpMethod.GET.matches(method) && isPublicGetPath(requestUri);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request); // 헤더에서 토큰 추출

        try {
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) { // 토큰 유뮤와 유효한지 체크
                Long userId = jwtTokenProvider.getUserId(token);
                UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // 인증 객체 생성
                SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 시스템에 유저 등록
            }
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            return null;
        }
        return bearerToken.substring(7);
    }

    // 공개 인증 경로 확인 함수
    private boolean isPublicAuthPath(String requestUri) {
        return "/api/auth/email/send".equals(requestUri)
                || "/api/auth/email/verify".equals(requestUri)
                || "/api/auth/signup".equals(requestUri)
                || "/api/auth/login".equals(requestUri)
                || "/api/auth/password/reset/request".equals(requestUri);
    }

    // 공개 조회 경로 확인 함수
    private boolean isPublicGetPath(String requestUri) {
        return "/api/categories".equals(requestUri)
                || "/api/documents".equals(requestUri)
                || "/api/documents/search".equals(requestUri)
                || "/api/search/trending".equals(requestUri)
                || requestUri.startsWith("/uploads/images/")
                || requestUri.startsWith("/api/documents/");
    }

    // PathPattern 매칭용 PathContainer 변환기
    private static final class PathContainerParser {

        private PathContainerParser() {
        }

        private static org.springframework.http.server.PathContainer from(String requestUri) {
            return org.springframework.http.server.PathContainer.parsePath(requestUri);
        }
    }
}
