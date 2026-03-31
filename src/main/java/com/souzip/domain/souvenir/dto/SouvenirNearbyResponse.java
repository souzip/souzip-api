package com.souzip.domain.souvenir.dto;

import com.souzip.domain.category.entity.Category;
import com.souzip.domain.souvenir.entity.Purpose;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Function;

public record SouvenirNearbyResponse(
        Long id,
        String name,
        Category category,
        Purpose purpose,
        int localPrice,
        int krwPrice,
        String currencySymbol,
        String thumbnail,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        long wishlistCount,
        Boolean isWishlisted
) {
    private static final int INDEX_ID = 0;
    private static final int INDEX_NAME = 1;
    private static final int INDEX_CATEGORY = 2;
    private static final int INDEX_PURPOSE = 3;
    private static final int INDEX_LOCAL_PRICE = 4;
    private static final int INDEX_KRW_PRICE = 5;
    private static final int INDEX_CURRENCY_SYMBOL = 6;
    private static final int INDEX_THUMBNAIL = 7;
    private static final int INDEX_LATITUDE = 8;
    private static final int INDEX_LONGITUDE = 9;
    private static final int INDEX_ADDRESS = 10;
    private static final int INDEX_WISHLIST_COUNT = 11;

    public static SouvenirNearbyResponse from(
            Long id,
            String name,
            Category category,
            Purpose purpose,
            int localPrice,
            int krwPrice,
            String currencySymbol,
            String thumbnail,
            BigDecimal latitude,
            BigDecimal longitude,
            String address,
            long wishlistCount,
            Boolean isWishlisted
    ) {
        return new SouvenirNearbyResponse(
                id,
                name,
                category,
                purpose,
                localPrice,
                krwPrice,
                currencySymbol,
                thumbnail,
                latitude,
                longitude,
                address,
                wishlistCount,
                isWishlisted
        );
    }

    public static SouvenirNearbyResponse fromObjectArray(
            Object[] row,
            Function<String, String> urlGenerator,
            Set<Long> wishlistedIds
    ) {
        Long id = extractId(row);
        String name = extractName(row);
        Category category = extractCategory(row);
        Purpose purpose = extractPurpose(row);
        int localPrice = extractLocalPrice(row);
        int krwPrice = extractKrwPrice(row);
        String currencySymbol = extractCurrencySymbol(row);
        String thumbnail = extractThumbnail(row);
        BigDecimal latitude = extractLatitude(row);
        BigDecimal longitude = extractLongitude(row);
        String address = extractAddress(row);

        String imageUrl = generateImageUrl(thumbnail, urlGenerator);
        long wishlistCount = ((Number) row[INDEX_WISHLIST_COUNT]).longValue();
        Boolean isWishlisted = wishlistedIds.isEmpty() ? null : wishlistedIds.contains(id);

        return new SouvenirNearbyResponse(
                id,
                name,
                category,
                purpose,
                localPrice,
                krwPrice,
                currencySymbol,
                imageUrl,
                latitude,
                longitude,
                address,
                wishlistCount,
                isWishlisted
        );
    }

    private static Long extractId(Object[] row) {
        return ((Number) row[INDEX_ID]).longValue();
    }

    private static String extractName(Object[] row) {
        return (String) row[INDEX_NAME];
    }

    private static Category extractCategory(Object[] row) {
        return Category.valueOf((String) row[INDEX_CATEGORY]);
    }

    private static Purpose extractPurpose(Object[] row) {
        return Purpose.valueOf((String) row[INDEX_PURPOSE]);
    }

    private static int extractLocalPrice(Object[] row) {
        return ((Number) row[INDEX_LOCAL_PRICE]).intValue();
    }

    private static int extractKrwPrice(Object[] row) {
        return ((Number) row[INDEX_KRW_PRICE]).intValue();
    }

    private static String extractCurrencySymbol(Object[] row) {
        return (String) row[INDEX_CURRENCY_SYMBOL];
    }

    private static String extractThumbnail(Object[] row) {
        return (String) row[INDEX_THUMBNAIL];
    }

    private static BigDecimal extractLatitude(Object[] row) {
        return (BigDecimal) row[INDEX_LATITUDE];
    }

    private static BigDecimal extractLongitude(Object[] row) {
        return (BigDecimal) row[INDEX_LONGITUDE];
    }

    private static String extractAddress(Object[] row) {
        return (String) row[INDEX_ADDRESS];
    }

    private static String generateImageUrl(String thumbnail, Function<String, String> urlGenerator) {
        if (hasThumbnail(thumbnail)) {
            return urlGenerator.apply(thumbnail);
        }
        return null;
    }

    private static boolean hasThumbnail(String thumbnail) {
        return thumbnail != null;
    }
}
