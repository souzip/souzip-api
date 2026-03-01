package com.souzip.api.application.geocoding;

import com.souzip.api.application.geocoding.dto.GeocodingResult;
import com.souzip.api.application.geocoding.provided.ReverseGeocoding;
import com.souzip.api.application.geocoding.required.AddressProvider;
import com.souzip.api.domain.shared.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class GeocodingService implements ReverseGeocoding {

    private final AddressProvider addressProvider;

    @Override
    public GeocodingResult getAddress(Coordinate coordinate) {
        return addressProvider.getAddress(coordinate);
    }
}
