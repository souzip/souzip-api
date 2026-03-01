package com.souzip.application.location.dto;

import com.souzip.domain.shared.Coordinate;

public record SearchPlace(
        String name,
        String address,
        Coordinate coordinate
) {
}
