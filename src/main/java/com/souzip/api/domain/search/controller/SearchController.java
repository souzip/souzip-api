package com.souzip.api.domain.search.controller;

import com.souzip.api.domain.search.dto.SearchResponse;
import com.souzip.api.domain.search.service.SearchService;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.common.dto.pagination.PaginationRequest;
import com.souzip.api.global.common.dto.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/search")
@RequiredArgsConstructor
@RestController
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/locations")
    public SuccessResponse<PaginationResponse<SearchResponse>> searchLocations(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        log.info("검색어: {}, 페이지 번호: {}, 페이지 크기: {}", keyword, pageNo, pageSize);
        PaginationRequest paginationRequest = new PaginationRequest(pageNo, pageSize);
        return SuccessResponse.of(searchService.search(keyword, paginationRequest));
    }
}
