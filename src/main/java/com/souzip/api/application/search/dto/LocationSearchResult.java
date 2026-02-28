package com.souzip.api.application.search.dto;

import com.souzip.api.domain.location.Location;

import java.util.List;

public record LocationSearchResult(
        List<Location> locations
) implements SearchResult {
}
