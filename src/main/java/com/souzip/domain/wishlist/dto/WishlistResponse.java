package com.souzip.domain.wishlist.dto;

public record WishlistResponse(
        Long souvenirId,
        boolean wishlisted
) {
    public static WishlistResponse of(Long souvenirId, boolean wishlisted) {
        return new WishlistResponse(souvenirId, wishlisted);
    }
}
