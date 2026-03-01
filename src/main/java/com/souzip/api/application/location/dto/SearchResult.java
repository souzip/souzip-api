package com.souzip.api.application.location.dto;

public sealed interface SearchResult
        permits CitySearchResult, PlaceSearchResult {
}
