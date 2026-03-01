package com.souzip.application.location.dto;

import com.souzip.domain.city.entity.City;
import java.util.List;

public record CitySearchResult(
        List<City> cities
) implements SearchResult {
}
