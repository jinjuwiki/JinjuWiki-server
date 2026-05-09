package com.jinju.jinjuwiki.domain.category.service;

import com.jinju.jinjuwiki.domain.category.dto.response.CategoryResponse;
import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.entity.CategoryType;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
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
    private final DocumentRepository documentRepository;

    @Override
    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        Map<String, Category> categoriesByName = categories.stream()
                .collect(Collectors.toMap(Category::getName, category -> category));

        Category schoolCategory = categoriesByName.get(CategoryType.SCHOOL.getDisplayName());
        Set<String> schoolChildNames = Set.copyOf(SCHOOL_CHILDREN);

        List<CategoryResponse> responses = new ArrayList<>();

        if (schoolCategory != null) {
            List<Document> schoolDocuments = documentRepository.findByCategoryIdOrderByTitleAsc(schoolCategory.getId());
            List<Category> schoolChildren = SCHOOL_CHILDREN.stream()
                    .map(categoriesByName::get)
                    .filter(Objects::nonNull)
                    .toList();

            schoolDocuments.stream()
                    .map(document -> CategoryResponse.parent(
                            schoolCategory.getId(),
                            document.getId(),
                            document.getTitle(),
                            schoolChildren.stream()
                                    .map(category -> CategoryResponse.leaf(category.getId(), document.getId(), category.getName()))
                                    .toList()
                    ))
                    .forEach(responses::add);
        }

        categories.stream()
                .filter(category -> !category.getName().equals(CategoryType.SCHOOL.getDisplayName()))
                .filter(category -> !schoolChildNames.contains(category.getName()))
                .map(category -> CategoryResponse.leaf(category.getId(), null, category.getName()))
                .forEach(responses::add);

        return responses;
    }
}
