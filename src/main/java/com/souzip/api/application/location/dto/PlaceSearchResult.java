package com.souzip.api.application.location.dto;

import java.util.List;

public record PlaceSearchResult(
        List<SearchPlace> places
) implements SearchResult {
}
