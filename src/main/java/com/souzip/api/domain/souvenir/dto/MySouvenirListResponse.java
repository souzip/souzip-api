package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.global.common.dto.pagination.PaginationResponse.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public record MySouvenirListResponse(
    List<MySouvenirResponse> content,
    PageInfo pagination
) {
    public static MySouvenirListResponse from(Page<MySouvenirResponse> page) {
        PageInfo pagination = PageInfo.of(page);
        return new MySouvenirListResponse(page.getContent(), pagination);
    }
}
