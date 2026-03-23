package com.souzip.domain.wishlist.controller;

import com.souzip.domain.wishlist.dto.MyWishlistListResponse;
import com.souzip.domain.wishlist.dto.WishlistResponse;
import com.souzip.domain.wishlist.service.WishlistService;
import com.souzip.global.common.dto.SuccessResponse;
import com.souzip.global.security.annotation.CurrentUserId;
import com.souzip.global.security.annotation.RequireAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/api/wishlists/{souvenirId}")
    @RequireAuth
    public SuccessResponse<WishlistResponse> addWishlist(
        @CurrentUserId Long currentUserId,
        @PathVariable Long souvenirId
    ) {
        return SuccessResponse.of(wishlistService.addWishlist(currentUserId, souvenirId));
    }

    @DeleteMapping("/api/wishlists/{souvenirId}")
    @RequireAuth
    public SuccessResponse<WishlistResponse> removeWishlist(
        @CurrentUserId Long currentUserId,
        @PathVariable Long souvenirId
    ) {
        return SuccessResponse.of(wishlistService.removeWishlist(currentUserId, souvenirId));
    }

    @GetMapping("/api/users/me/wishlists")
    @RequireAuth
    public SuccessResponse<MyWishlistListResponse> getMyWishlist(
        @CurrentUserId Long currentUserId,
        Pageable pageable
    ) {
        return SuccessResponse.of(wishlistService.getMyWishlist(currentUserId, pageable));
    }
}
