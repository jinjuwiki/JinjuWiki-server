package com.jinju.jinjuwiki.global.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 업로드 정적 리소스 매핑
@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.image-path}")
    private String imageUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteUploadPath = Path.of(imageUploadPath).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(absoluteUploadPath);
    }

    // Swagger 기본 진입 경로 리다이렉트 설정
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger-ui", "/swagger-ui/index.html");
        registry.addRedirectViewController("/swagger-ui/", "/swagger-ui/index.html");
    }
}
