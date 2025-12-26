package com.souzip.api.domain.geocoding.controller;

import com.souzip.api.domain.geocoding.dto.GeocodingAddressResponse;
import com.souzip.api.domain.geocoding.service.GeocodingService;
import com.souzip.api.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
public class GeocodingController {

    private final GeocodingService geocodingService;

    @GetMapping("/address")
    public SuccessResponse<GeocodingAddressResponse> getAddress(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        return SuccessResponse.of(geocodingService.getAddress(latitude, longitude));
    }
}
