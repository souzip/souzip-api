package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.souvenir.entity.Purpose;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record SouvenirCreateRequest(
        String name,
        Integer localPrice,
        String localCurrency,
        Integer krwPrice,
        String description,
        String address,
        String locationDetail,
        BigDecimal latitude,
        BigDecimal longitude,
        Category category,
        Purpose purpose,
        List<MultipartFile> files
) {}
