package com.souzip.domain.category.controller;

import com.souzip.domain.category.dto.CategoriesResponse;
import com.souzip.domain.category.service.CategoryService;
import com.souzip.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/categories")
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public SuccessResponse<CategoriesResponse> getAllCategories() {
        CategoriesResponse response = categoryService.getAllCategories();
        return SuccessResponse.of(response);
    }
}
