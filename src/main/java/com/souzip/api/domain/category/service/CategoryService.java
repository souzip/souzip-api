package com.souzip.api.domain.category.service;

import com.souzip.api.domain.category.dto.CategoriesResponse;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.category.entity.Category;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CategoryService {

    public CategoriesResponse getAllCategories() {
        List<CategoryDto> categories = Arrays.stream(Category.values())
            .map(CategoryDto::from)
            .toList();

        return CategoriesResponse.of(categories);
    }
}
