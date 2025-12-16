package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.souvenir.entity.Purpose;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record SouvenirCreateRequestDto(
        String name,
        Integer price,
        String description,
        Category category,
        Purpose purpose,
        Long cityId,
        List<MultipartFile> files
) {}
