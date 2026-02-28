package com.souzip.api.application.search;

import com.souzip.api.application.search.dto.CitySearchResult;
import com.souzip.api.application.search.dto.LocationSearchResult;
import com.souzip.api.application.search.dto.SearchResult;
import com.souzip.api.application.search.provided.LocationSearch;
import com.souzip.api.application.search.required.CitySearchRepository;
import com.souzip.api.application.search.required.LocationRepository;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.location.Location;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.util.Assert.hasText;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SearchService implements LocationSearch {

    private final CitySearchRepository cityRepository;
    private final LocationRepository locationRepository;

    @Override
    public SearchResult search(String keyword) {
        hasText(keyword, "검색어를 입력해주세요.");

        return searchCities(keyword)
                .or(() -> searchLocations(keyword))
                .orElseGet(() -> new CitySearchResult(List.of()));
    }

    private Optional<SearchResult> searchCities(String keyword) {
        List<City> cities = cityRepository.searchByKeyword(keyword);

        if (cities.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new CitySearchResult(cities));
    }

    private Optional<SearchResult> searchLocations(String keyword) {
        List<Location> locations = locationRepository.findByNameContaining(keyword);

        if (locations.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new LocationSearchResult(locations));
    }
}
