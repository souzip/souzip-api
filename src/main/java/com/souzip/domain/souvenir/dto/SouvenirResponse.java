package com.souzip.domain.souvenir.dto;

import com.souzip.domain.category.entity.Category;
import com.souzip.application.file.dto.FileResponse;
import com.souzip.domain.souvenir.entity.Purpose;
import com.souzip.domain.souvenir.entity.Souvenir;

import java.math.BigDecimal;
import java.util.List;

public record SouvenirResponse(
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
    List<FileResponse> files
) {
    public static SouvenirResponse of(Souvenir souvenir, List<FileResponse> files) {
        return new SouvenirResponse(
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
            files
        );
    }

    public static SouvenirResponse of(
        Souvenir souvenir,
        List<FileResponse> files,
        PriceResponse priceResponse
    ) {
        return new SouvenirResponse(
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
            files
        );
    }
}
