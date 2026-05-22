package com.souzip.adapter.webapi.location;

import com.souzip.adapter.webapi.location.dto.AddressResponse;
import com.souzip.application.location.dto.AddressResult;
import com.souzip.application.location.provided.ReverseGeocoding;
import com.souzip.domain.shared.Coordinate;
import com.souzip.global.common.dto.SuccessResponse;
import com.souzip.global.security.annotation.RequireAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @deprecated 2026-03-15부터 삭제 예정. {@link LocationApi} 사용 권장
 */
@Deprecated(since = "2026-03-01", forRemoval = true)
@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
@RestController
public class GeocodingApi {

    private final ReverseGeocoding reverseGeocoding;

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
}
