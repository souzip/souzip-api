package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.souvenir.entity.Purpose;
import com.souzip.api.domain.souvenir.entity.Souvenir;

import java.math.BigDecimal;
import java.util.List;

public record SouvenirDetailResponse(
    Long id,
    String name,

    Integer localPrice,
    String currencySymbol,
    Integer krwPrice,

    PriceResponse price,

    String description,
    String address,
    String locationDetail,
    BigDecimal latitude,
    BigDecimal longitude,
    Category category,
    Purpose purpose,
    String countryCode,
    String userNickname,
    String userProfileImageUrl,
    Boolean isOwned,
    List<FileResponse> files
) {
    public static SouvenirDetailResponse of(
        Souvenir souvenir,
        List<FileResponse> files,
        Boolean isOwned
    ) {
        return new SouvenirDetailResponse(
            souvenir.getId(),
            souvenir.getName(),
            souvenir.getLocalPrice(),
            souvenir.getCurrencySymbol(),
            souvenir.getKrwPrice(),
            null,
            souvenir.getDescription(),
            souvenir.getAddress(),
            souvenir.getLocationDetail(),
            souvenir.getLatitude(),
            souvenir.getLongitude(),
            souvenir.getCategory(),
            souvenir.getPurpose(),
            souvenir.getCountryCode(),
            souvenir.getUser().getNickname(),
            souvenir.getUser().getProfileImageUrl(),
            isOwned,
            files
        );
    }

    public static SouvenirDetailResponse of(
        Souvenir souvenir,
        List<FileResponse> files,
        Boolean isOwned,
        PriceResponse priceResponse
    ) {
        return new SouvenirDetailResponse(
            souvenir.getId(),
            souvenir.getName(),
            souvenir.getLocalPrice(),
            souvenir.getCurrencySymbol(),
            souvenir.getKrwPrice(),
            priceResponse,
            souvenir.getDescription(),
            souvenir.getAddress(),
            souvenir.getLocationDetail(),
            souvenir.getLatitude(),
            souvenir.getLongitude(),
            souvenir.getCategory(),
            souvenir.getPurpose(),
            souvenir.getCountryCode(),
            souvenir.getUser().getNickname(),
            souvenir.getUser().getProfileImageUrl(),
            isOwned,
            files
        );
    }
}
