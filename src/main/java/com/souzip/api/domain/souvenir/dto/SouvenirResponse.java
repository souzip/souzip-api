package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.souvenir.entity.Souvenir;

import java.math.BigDecimal;
import java.util.List;

public record SouvenirResponse(
        Long id,
        String name,
        Integer localPrice,
        String currencySymbol,
        Integer krwPrice,
        String description,
        String address,
        String locationDetail,
        BigDecimal latitude,
        BigDecimal longitude,
        CategoryDto category,
        PurposeDto purpose,
        String countryCode,
        String userNickname,
        String userProfileImageUrl,
        Boolean isOwned,
        List<FileResponse> files
) {

    public static SouvenirResponse of(
            Souvenir souvenir,
            List<FileResponse> files,
            Boolean isOwned
    ) {
        return new SouvenirResponse(
                souvenir.getId(),
                souvenir.getName(),
                souvenir.getLocalPrice(),
                souvenir.getCurrencySymbol(),
                souvenir.getKrwPrice(),
                souvenir.getDescription(),
                souvenir.getAddress(),
                souvenir.getLocationDetail(),
                souvenir.getLatitude(),
                souvenir.getLongitude(),
                CategoryDto.from(souvenir.getCategory()),
                PurposeDto.from(souvenir.getPurpose()),
                souvenir.getCountryCode(),
                souvenir.getUser().getNickname(),
                souvenir.getUser().getProfileImageUrl(),
                isOwned,
                files
        );
    }
}
