package com.souzip.api.application.search.dto;

import java.util.List;

public record PlaceSearchResult(
        List<SearchPlace> places
) implements SearchResult {
}
