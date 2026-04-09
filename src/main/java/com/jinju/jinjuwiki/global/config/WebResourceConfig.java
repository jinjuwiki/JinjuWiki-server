package com.jinju.jinjuwiki.global.config;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 업로드 정적 리소스 매핑
@Configuration
@RequiredArgsConstructor
public class WebResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.image-path}")
    private String imageUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteUploadPath = Path.of(imageUploadPath).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(absoluteUploadPath);
    }
}
