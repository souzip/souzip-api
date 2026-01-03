package com.souzip.api.domain.souvenir.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record MySouvenirListResponse(
    List<MySouvenirResponse> souvenirs,
    PaginationInfo pagination
) {
    public record PaginationInfo(
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext
    ) {}

    public static MySouvenirListResponse from(Page<MySouvenirResponse> page) {
        PaginationInfo pagination = new PaginationInfo(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext()
        );
        return new MySouvenirListResponse(page.getContent(), pagination);
    }
}
