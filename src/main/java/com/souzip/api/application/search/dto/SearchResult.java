package com.souzip.api.application.search.dto;

public sealed interface SearchResult
        permits CitySearchResult, PlaceSearchResult {
}
