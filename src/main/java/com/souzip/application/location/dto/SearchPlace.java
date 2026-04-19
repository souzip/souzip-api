package com.souzip.application.location.dto;

import com.souzip.shared.domain.Coordinate;

public record SearchPlace(
        String name,
        String address,
        String region,
        String category,
        Coordinate coordinate
) {
}
