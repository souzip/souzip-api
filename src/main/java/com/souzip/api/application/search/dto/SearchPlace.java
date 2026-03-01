package com.souzip.api.application.search.dto;

import com.souzip.api.domain.shared.Coordinate;

public record SearchPlace(
        String name,
        String address,
        Coordinate coordinate
) {
}
