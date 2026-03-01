package com.souzip.api.application.location.required;

import com.souzip.api.application.location.dto.AddressResult;
import com.souzip.api.domain.shared.Coordinate;

public interface AddressProvider {

    AddressResult getAddress(Coordinate coordinate);
}
