package com.souzip.application.location.required;

import com.souzip.application.location.dto.AddressResult;
import com.souzip.domain.shared.Coordinate;

public interface AddressProvider {

    AddressResult getAddress(Coordinate coordinate);
}
