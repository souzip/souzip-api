package com.souzip.api.application.location;

import com.souzip.api.application.location.dto.AddressResult;
import com.souzip.api.application.location.provided.ReverseGeocoding;
import com.souzip.api.application.location.required.AddressProvider;
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
    public AddressResult getAddress(Coordinate coordinate) {
        return addressProvider.getAddress(coordinate);
    }
}
