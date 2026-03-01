package com.souzip.application.location.dto;

public sealed interface SearchResult
        permits CitySearchResult, PlaceSearchResult {
}
