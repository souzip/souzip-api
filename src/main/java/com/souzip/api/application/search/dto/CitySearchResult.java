package com.souzip.api.application.search.dto;

import com.souzip.api.domain.city.entity.City;
import java.util.List;

public record CitySearchResult(
        List<City> cities
) implements SearchResult {
}
