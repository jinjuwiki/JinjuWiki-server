package com.jinju.jinjuwiki.global.config;

import com.jinju.jinjuwiki.global.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 공개 API 경로 상수
    private static final String AUTH_EMAIL_SEND_PATH = "/api/auth/email/send";
    private static final String AUTH_EMAIL_VERIFY_PATH = "/api/auth/email/verify";
    private static final String AUTH_SIGNUP_PATH = "/api/auth/signup";
    private static final String AUTH_LOGIN_PATH = "/api/auth/login";
    private static final String CATEGORY_LIST_PATH = "/api/categories";
    private static final String DOCUMENT_LIST_PATH = "/api/documents";
    private static final String DOCUMENT_DETAIL_PATH = "/api/documents/*";
    private static final String DOCUMENT_SEARCH_PATH = "/api/documents/search";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, AUTH_EMAIL_SEND_PATH, AUTH_EMAIL_VERIFY_PATH).permitAll()
                        .requestMatchers(HttpMethod.POST, AUTH_SIGNUP_PATH, AUTH_LOGIN_PATH).permitAll()
                        .requestMatchers(HttpMethod.GET, CATEGORY_LIST_PATH).permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, DOCUMENT_LIST_PATH, DOCUMENT_SEARCH_PATH, DOCUMENT_DETAIL_PATH).permitAll()
                        .requestMatchers(HttpMethod.POST, DOCUMENT_LIST_PATH).authenticated()
                        .requestMatchers(HttpMethod.PUT, DOCUMENT_DETAIL_PATH).authenticated()
                        .requestMatchers(HttpMethod.DELETE, DOCUMENT_DETAIL_PATH).authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.ngrok-free.app",
                "https://*.ngrok.io"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
