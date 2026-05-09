package com.jinju.jinjuwiki.domain.category.service;

import com.jinju.jinjuwiki.domain.category.dto.response.CategoryResponse;
import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.entity.CategoryType;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final List<String> SCHOOL_CHILDREN = List.of(
            CategoryType.STUDENT.getDisplayName(),
            CategoryType.TEACHER.getDisplayName(),
            CategoryType.INCIDENT.getDisplayName()
    );

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        Map<String, Category> categoriesByName = categories.stream()
                .collect(Collectors.toMap(Category::getName, category -> category));

        Category schoolCategory = categoriesByName.get(CategoryType.SCHOOL.getDisplayName());
        Set<String> schoolChildNames = Set.copyOf(SCHOOL_CHILDREN);

        List<CategoryResponse> responses = new ArrayList<>();

        if (schoolCategory != null) {
            List<CategoryResponse> schoolChildren = SCHOOL_CHILDREN.stream()
                    .map(categoriesByName::get)
                    .filter(Objects::nonNull)
                    .map(category -> CategoryResponse.leaf(category.getId(), category.getName()))
                    .toList();

            responses.add(CategoryResponse.parent(
                    schoolCategory.getId(),
                    schoolCategory.getName(),
                    schoolChildren
            ));
        }

        categories.stream()
                .filter(category -> !category.getName().equals(CategoryType.SCHOOL.getDisplayName()))
                .filter(category -> !schoolChildNames.contains(category.getName()))
                .map(category -> CategoryResponse.leaf(category.getId(), category.getName()))
                .forEach(responses::add);

        return responses;
    }
}
