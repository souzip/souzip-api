package com.souzip.domain.search.dto;

import com.souzip.global.common.dto.pagination.PaginationResponse;

public class SearchListResponse extends PaginationResponse<SearchResponse> {

    private SearchListResponse(PaginationResponse<SearchResponse> response) {
        super(response.getContent(), response.getPagination());
    }

    public static SearchListResponse from(PaginationResponse<SearchResponse> response) {
        return new SearchListResponse(response);
    }
}
