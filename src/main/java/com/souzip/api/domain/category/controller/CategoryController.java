package com.souzip.api.domain.category.controller;

import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.category.service.CategoryService;
import com.souzip.api.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/categories")
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public SuccessResponse<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return SuccessResponse.of(categories);
    }
}
