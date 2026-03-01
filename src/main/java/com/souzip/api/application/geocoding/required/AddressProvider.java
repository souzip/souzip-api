package com.souzip.api.application.geocoding.required;

import com.souzip.api.application.geocoding.dto.GeocodingResult;
import com.souzip.api.domain.shared.Coordinate;

public interface AddressProvider {

    GeocodingResult getAddress(Coordinate coordinate);
}
