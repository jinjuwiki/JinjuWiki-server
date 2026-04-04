package com.jinju.jinjuwiki.domain.category.config;

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

    private final CategoryRepository categoryRepository;

    @Bean
    public CommandLineRunner initCategories() {
        return args -> {
            List<String> defaultCategories = List.of("학교", "공부", "생활", "입시", "기타");
            List<Category> categories = defaultCategories.stream()
                    .filter(name -> !categoryRepository.existsByName(name))
                    .map(name -> Category.builder().name(name).build())
                    .toList();

            if (!categories.isEmpty()) {
                categoryRepository.saveAll(categories);
            }
        };
    }
}
