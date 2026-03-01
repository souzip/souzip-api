package com.souzip.api.application.geocoding.provided;

import com.souzip.api.application.geocoding.dto.GeocodingResult;
import com.souzip.api.domain.shared.Coordinate;

public interface ReverseGeocoding {

    GeocodingResult getAddress(Coordinate coordinate);
}
