package com.souzip.api.application.search.dto;

import java.math.BigDecimal;

public record Place(
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {}
