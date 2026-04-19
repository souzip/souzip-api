package com.souzip.adapter.webapi.location;

import com.souzip.adapter.webapi.location.dto.AddressResponse;
import com.souzip.adapter.webapi.location.dto.SearchResponse;
import com.souzip.application.location.dto.AddressResult;
import com.souzip.application.location.dto.SearchResult;
import com.souzip.application.location.provided.LocationSearch;
import com.souzip.application.location.provided.ReverseGeocoding;
import com.souzip.auth.adapter.security.annotation.RequireAuth;
import com.souzip.domain.shared.Coordinate;
import com.souzip.shared.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequestMapping("/api/location")
@RequiredArgsConstructor
@RestController
public class LocationApi {

    private final ReverseGeocoding reverseGeocoding;
    private final LocationSearch locationSearch;

    @RequireAuth
    @GetMapping("/address")
    public SuccessResponse<AddressResponse> getAddress(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        Coordinate coordinate = Coordinate.of(
                BigDecimal.valueOf(latitude),
                BigDecimal.valueOf(longitude)
        );

        AddressResult result = reverseGeocoding.getAddress(coordinate);

        AddressResponse response = AddressResponse.from(result);

        return SuccessResponse.of(response);
    }

    @GetMapping("/search")
    public SuccessResponse<List<SearchResponse>> search(@RequestParam String keyword) {
        log.info("검색어: {}", keyword);

        SearchResult result = locationSearch.search(keyword);

        List<SearchResponse> response = SearchResponse.from(result);

        return SuccessResponse.of(response);
    }
}
