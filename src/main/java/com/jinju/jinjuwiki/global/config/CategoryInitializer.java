package com.jinju.jinjuwiki.global.config;

import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.entity.CategoryType;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CategoryInitializer {

    // 기본 카테고리 이름 목록
    private static final List<String> DEFAULT_CATEGORIES = List.of(CategoryType.values()).stream()
            .map(CategoryType::getDisplayName)
            .toList();

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
