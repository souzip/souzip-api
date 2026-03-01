package com.souzip.api.adapter.webapi.search;

import com.souzip.api.adapter.webapi.search.dto.SearchResponse;
import com.souzip.api.application.search.dto.SearchResult;
import com.souzip.api.application.search.provided.LocationSearch;
import com.souzip.api.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/api/search")
@RequiredArgsConstructor
@RestController
public class SearchApi {

    private final LocationSearch locationSearch;

    @GetMapping
    public SuccessResponse<List<SearchResponse>> search(@RequestParam String keyword) {
        log.info("검색어: {}", keyword);

        SearchResult result = locationSearch.search(keyword);

        List<SearchResponse> response = SearchResponse.from(result);

        return SuccessResponse.of(response);
    }
}
