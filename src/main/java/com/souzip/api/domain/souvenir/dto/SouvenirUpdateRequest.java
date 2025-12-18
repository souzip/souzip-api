package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.souvenir.entity.Purpose;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record SouvenirUpdateRequest(
        String name,
        Integer localPrice,
        String currencySymbol,
        Integer krwPrice,
        String description,
        String address,
        String locationDetail,
        BigDecimal latitude,
        BigDecimal longitude,
        Category category,
        Purpose purpose,
        String countryCode,
        List<MultipartFile> files
) {}
