package com.jinju.jinjuwiki.global.config;

import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CategoryInitializer {

    // 상수화
    private static final List<String> DEFAULT_CATEGORIES = List.of("학교", "학생", "사건사고", "선생님", "기타");

    private final CategoryRepository categoryRepository;

    @Bean
    public CommandLineRunner initCategories() {
        return args -> {
            List<Category> categories = DEFAULT_CATEGORIES.stream()
                    .filter(name -> !categoryRepository.existsByName(name))
                    .map(name -> Category.builder().name(name).build())
                    .toList();

            if (!categories.isEmpty()) {
                categoryRepository.saveAll(categories);
            }
        };
    }
}
