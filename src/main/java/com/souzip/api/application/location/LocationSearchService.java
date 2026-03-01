package com.souzip.api.application.location;

import com.souzip.api.application.location.dto.CitySearchResult;
import com.souzip.api.application.location.dto.PlaceSearchResult;  // ← 추가
import com.souzip.api.application.location.dto.SearchPlace;  // ← 추가
import com.souzip.api.application.location.dto.SearchResult;
import com.souzip.api.application.location.provided.LocationSearch;
import com.souzip.api.application.location.required.CitySearchRepository;
import com.souzip.api.application.location.required.PlaceSearchProvider;  // ← 추가
import com.souzip.api.domain.city.entity.City;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.util.Assert.hasText;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class LocationSearchService implements LocationSearch {

    private static final int DEFAULT_CITY_LIMIT = 10;

    private final CitySearchRepository cityRepository;
    private final PlaceSearchProvider placeSearchProvider;

    @Override
    public SearchResult search(String keyword) {
        hasText(keyword, "검색어를 입력해주세요.");

        return searchCities(keyword)
                .or(() -> searchPlaces(keyword))
                .orElseGet(() -> new PlaceSearchResult(List.of()));
    }

    private Optional<SearchResult> searchCities(String keyword) {
        Pageable pageable = PageRequest.of(0, DEFAULT_CITY_LIMIT);
        List<City> cities = cityRepository.searchByKeyword(keyword, pageable);

        if (cities.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new CitySearchResult(cities));
    }

    private Optional<SearchResult> searchPlaces(String keyword) {
        List<SearchPlace> places = placeSearchProvider.searchByKeyword(keyword);

        if (places.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new PlaceSearchResult(places));
    }
}
