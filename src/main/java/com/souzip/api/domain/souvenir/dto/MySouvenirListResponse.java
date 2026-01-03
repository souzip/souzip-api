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
        long totalItems,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
    ) {}

    public static MySouvenirListResponse from(Page<MySouvenirResponse> page) {
        PaginationInfo pagination = new PaginationInfo(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext(),
            page.hasPrevious()
        );
        return new MySouvenirListResponse(page.getContent(), pagination);
    }
}
