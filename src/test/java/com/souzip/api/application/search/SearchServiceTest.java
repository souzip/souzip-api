package com.souzip.api.application.search;

import com.souzip.api.application.search.dto.CitySearchResult;
import com.souzip.api.application.search.dto.LocationSearchResult;
import com.souzip.api.application.search.dto.SearchResult;
import com.souzip.api.application.search.required.CitySearchRepository;
import com.souzip.api.application.search.required.LocationRepository;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import com.souzip.api.domain.location.Location;
import com.souzip.api.domain.location.LocationCreateRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private CitySearchRepository cityRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationSearchService searchService;

    @Test
    void searchCities() {
        String keyword = "도쿄";
        City city = createCity("도쿄", "Tokyo");

        given(cityRepository.searchByKeyword(keyword))
                .willReturn(List.of(city));

        SearchResult result = searchService.search(keyword);

        assertThat(result).isInstanceOf(CitySearchResult.class);
        CitySearchResult cityResult = (CitySearchResult) result;
        assertThat(cityResult.cities()).hasSize(1);
        assertThat(cityResult.cities().getFirst().getNameKr()).isEqualTo("도쿄");

        then(locationRepository).shouldHaveNoInteractions();
    }

    @Test
    void searchLocations() {
        String keyword = "강남역";
        Location location = createLocation("강남역");

        given(cityRepository.searchByKeyword(keyword))
                .willReturn(List.of());
        given(locationRepository.findByNameContaining(keyword))
                .willReturn(List.of(location));

        SearchResult result = searchService.search(keyword);

        assertThat(result).isInstanceOf(LocationSearchResult.class);
        LocationSearchResult locationResult = (LocationSearchResult) result;
        assertThat(locationResult.locations()).hasSize(1);
        assertThat(locationResult.locations().getFirst().getName()).isEqualTo("강남역");
    }

    @Test
    void searchEmptyResult() {
        String keyword = "존재하지않는곳";

        given(cityRepository.searchByKeyword(keyword))
                .willReturn(List.of());
        given(locationRepository.findByNameContaining(keyword))
                .willReturn(List.of());

        SearchResult result = searchService.search(keyword);

        assertThat(result).isInstanceOf(CitySearchResult.class);
        CitySearchResult cityResult = (CitySearchResult) result;
        assertThat(cityResult.cities()).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void searchWithInvalidKeyword(String keyword) {
        assertThatThrownBy(() -> searchService.search(keyword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("검색어를 입력해주세요");

        then(cityRepository).shouldHaveNoInteractions();
        then(locationRepository).shouldHaveNoInteractions();
    }

    @Test
    void searchPrioritizesCity() {
        String keyword = "도쿄";
        City city = createCity("도쿄", "Tokyo");

        given(cityRepository.searchByKeyword(keyword))
                .willReturn(List.of(city));

        SearchResult result = searchService.search(keyword);

        assertThat(result).isInstanceOf(CitySearchResult.class);
        then(locationRepository).shouldHaveNoInteractions();
    }

    @Test
    void searchMultipleCities() {
        String keyword = "일본";
        City tokyo = createCity("도쿄", "Tokyo");
        City osaka = createCity("오사카", "Osaka");

        given(cityRepository.searchByKeyword(keyword))
                .willReturn(List.of(tokyo, osaka));

        SearchResult result = searchService.search(keyword);

        assertThat(result).isInstanceOf(CitySearchResult.class);
        CitySearchResult cityResult = (CitySearchResult) result;
        assertThat(cityResult.cities()).hasSize(2);
    }

    private City createCity(String nameKr, String nameEn) {
        Country country = createCountry("일본", "Japan");

        return City.create(
                nameEn,
                nameKr,
                BigDecimal.valueOf(35.6762),
                BigDecimal.valueOf(139.6503),
                country
        );
    }

    private Country createCountry(String nameKr, String nameEn) {
        return Country.of(
                nameEn,
                nameKr,
                "JP",
                "Tokyo",
                Region.ASIA,
                "https://example.com/japan.png",
                BigDecimal.valueOf(35.6762),
                BigDecimal.valueOf(139.6503),
                null
        );
    }

    private Location createLocation(String name) {
        return Location.create(new LocationCreateRequest(
                name,
                "서울특별시 강남구",
                BigDecimal.valueOf(37.4979),
                BigDecimal.valueOf(127.0276)
        ));
    }
}
