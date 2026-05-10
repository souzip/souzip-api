package com.souzip.application.location;

import com.souzip.application.location.dto.AddressResult;
import com.souzip.application.location.provided.ReverseGeocoding;
import com.souzip.application.location.required.AddressProvider;
import com.souzip.shared.domain.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReverseGeocodingService implements ReverseGeocoding {

    private final AddressProvider addressProvider;

    @Override
    public AddressResult getAddress(Coordinate coordinate) {
        return addressProvider.getAddress(coordinate);
    }
}
