package com.souzip.domain.wishlist.dto;

import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.wishlist.entity.Wishlist;

import java.time.LocalDateTime;

public record MyWishlistResponse(
        Long souvenirId,
        String name,
        String countryCode,
        String thumbnailUrl,
        LocalDateTime wishedAt
) {
    public static MyWishlistResponse of(Wishlist wishlist, String thumbnailUrl) {
        Souvenir s = wishlist.getSouvenir();
        return new MyWishlistResponse(
                s.getId(),
                s.getName(),
                s.getCountryCode(),
                thumbnailUrl,
                wishlist.getCreatedAt()
        );
    }
}
