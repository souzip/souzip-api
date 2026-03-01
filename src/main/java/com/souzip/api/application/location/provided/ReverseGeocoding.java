package com.souzip.api.application.location.provided;

import com.souzip.api.application.location.dto.AddressResult;
import com.souzip.api.domain.shared.Coordinate;

public interface ReverseGeocoding {

    AddressResult getAddress(Coordinate coordinate);
}
