package com.souzip.api.adapter.webapi.geocoding;

import com.souzip.api.adapter.webapi.geocoding.dto.GeocodingAddressResponse;
import com.souzip.api.application.geocoding.dto.GeocodingResult;
import com.souzip.api.application.geocoding.provided.ReverseGeocoding;
import com.souzip.api.domain.shared.Coordinate;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.RequireAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
@RestController
public class GeocodingApi {

    private final ReverseGeocoding reverseGeocoding;

    @RequireAuth
    @GetMapping("/address")
    public SuccessResponse<GeocodingAddressResponse> getAddress(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        Coordinate coordinate = Coordinate.of(
                BigDecimal.valueOf(latitude),
                BigDecimal.valueOf(longitude)
        );

        GeocodingResult result = reverseGeocoding.getAddress(coordinate);

        GeocodingAddressResponse response = GeocodingAddressResponse.from(result);

        return SuccessResponse.of(response);
    }
}
