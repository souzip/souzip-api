package com.souzip.application.location.provided;

import com.souzip.application.location.dto.AddressResult;
import com.souzip.domain.shared.Coordinate;

public interface ReverseGeocoding {

    AddressResult getAddress(Coordinate coordinate);
}
