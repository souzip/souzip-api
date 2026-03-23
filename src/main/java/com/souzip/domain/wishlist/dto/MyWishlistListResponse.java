package com.souzip.domain.wishlist.dto;

import com.souzip.global.common.dto.pagination.PaginationResponse.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public record MyWishlistListResponse(
        List<MyWishlistResponse> content,
        PageInfo pagination
) {
    public static MyWishlistListResponse from(Page<MyWishlistResponse> page) {
        return new MyWishlistListResponse(page.getContent(), PageInfo.of(page));
    }
}
