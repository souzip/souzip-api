package com.souzip.api.adapter.webapi.location;

import com.souzip.api.adapter.webapi.location.dto.AddressResponse;
import com.souzip.api.adapter.webapi.location.dto.SearchResponse;
import com.souzip.api.application.location.dto.*;
import com.souzip.api.application.location.provided.LocationSearch;
import com.souzip.api.application.location.provided.ReverseGeocoding;
import com.souzip.api.domain.shared.Coordinate;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.RequireAuth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
