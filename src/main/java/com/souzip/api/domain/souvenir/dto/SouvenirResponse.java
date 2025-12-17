package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.souvenir.entity.Purpose;
import com.souzip.api.domain.souvenir.entity.Souvenir;

import java.math.BigDecimal;
import java.util.List;

public record SouvenirResponse(
        Long id,
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
        List<FileResponse> files
) {

    public static SouvenirResponse from(Souvenir souvenir, List<FileResponse> files) {
        return new SouvenirResponse(
                souvenir.getId(),
                souvenir.getName(),
                souvenir.getLocalPrice(),
                souvenir.getLocalCurrency(),
                souvenir.getKrwPrice(),
                souvenir.getDescription(),
                souvenir.getAddress(),
                souvenir.getLocationDetail(),
                souvenir.getLatitude(),
                souvenir.getLongitude(),
                souvenir.getCategory(),
                souvenir.getPurpose(),
                files
        );
    }
}
