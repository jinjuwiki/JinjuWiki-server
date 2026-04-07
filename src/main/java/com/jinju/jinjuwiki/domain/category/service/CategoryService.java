package com.jinju.jinjuwiki.domain.category.service;

import com.jinju.jinjuwiki.domain.category.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getCategories();
}
