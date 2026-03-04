package com.souzip.application.location;

import com.souzip.application.location.dto.CitySearchResult;
import com.souzip.application.location.dto.SearchPlace;
import com.souzip.application.location.dto.PlaceSearchResult;
import com.souzip.application.location.dto.SearchResult;
import com.souzip.application.location.required.CitySearchRepository;
import com.souzip.application.location.required.PlaceSearchProvider;
import com.souzip.domain.city.entity.City;
import com.souzip.domain.country.entity.Country;
import com.souzip.domain.country.entity.Region;
import com.souzip.domain.shared.Coordinate;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LocationSearchServiceTest {

    @Mock
    private CitySearchRepository cityRepository;

    @Mock
    private PlaceSearchProvider placeSearchProvider;

    @InjectMocks
    private LocationSearchService searchService;

    @DisplayName("도시를 검색하면 CitySearchResult를 반환한다")
    @Test
    void searchCities() {
        // given
        String keyword = "도쿄";
        City city = createCity("도쿄", "Tokyo");

        given(cityRepository.searchByKeyword(anyString(), any(Pageable.class)))
                .willReturn(List.of(city));

        // when
        SearchResult result = searchService.search(keyword);

        // then
        assertThat(result).isInstanceOf(CitySearchResult.class);
        CitySearchResult cityResult = (CitySearchResult) result;
        assertThat(cityResult.cities()).hasSize(1);
        assertThat(cityResult.cities().getFirst().getNameKr()).isEqualTo("도쿄");

        then(placeSearchProvider).shouldHaveNoInteractions();
    }

    @DisplayName("도시가 없으면 Places를 검색하여 PlaceSearchResult를 반환한다")
    @Test
    void searchPlaces() {
        // given
        String keyword = "강남역 카페";
        SearchPlace place = createPlace("스타벅스 강남역점", "서울 강남구");

        given(cityRepository.searchByKeyword(anyString(), any(Pageable.class)))
                .willReturn(List.of());
        given(placeSearchProvider.searchByKeyword(keyword))
                .willReturn(List.of(place));

        // when
        SearchResult result = searchService.search(keyword);

        // then
        assertThat(result).isInstanceOf(PlaceSearchResult.class);
        PlaceSearchResult placeResult = (PlaceSearchResult) result;
        assertThat(placeResult.places()).hasSize(1);
        assertThat(placeResult.places().getFirst().name()).isEqualTo("스타벅스 강남역점");
    }

    @DisplayName("도시와 장소 모두 없으면 빈 PlaceSearchResult를 반환한다")
    @Test
    void searchEmptyResult() {
        // given
        String keyword = "존재하지않는곳";

        given(cityRepository.searchByKeyword(anyString(), any(Pageable.class)))
                .willReturn(List.of());
        given(placeSearchProvider.searchByKeyword(keyword))
                .willReturn(List.of());

        // when
        SearchResult result = searchService.search(keyword);

        // then
        assertThat(result).isInstanceOf(PlaceSearchResult.class);
        PlaceSearchResult placeResult = (PlaceSearchResult) result;
        assertThat(placeResult.places()).isEmpty();
    }

    @DisplayName("검색어가 유효하지 않으면 예외가 발생한다")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void searchWithInvalidKeyword(String keyword) {
        // when & then
        assertThatThrownBy(() -> searchService.search(keyword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("검색어를 입력해주세요");

        then(cityRepository).shouldHaveNoInteractions();
        then(placeSearchProvider).shouldHaveNoInteractions();
    }

    @DisplayName("도시를 찾으면 Places 검색을 하지 않는다")
    @Test
    void searchPrioritizesCity() {
        // given
        String keyword = "도쿄";
        City city = createCity("도쿄", "Tokyo");

        given(cityRepository.searchByKeyword(anyString(), any(Pageable.class)))
                .willReturn(List.of(city));

        // when
        SearchResult result = searchService.search(keyword);

        // then
        assertThat(result).isInstanceOf(CitySearchResult.class);
        then(placeSearchProvider).shouldHaveNoInteractions();
    }

    @DisplayName("여러 도시를 검색할 수 있다")
    @Test
    void searchMultipleCities() {
        // given
        String keyword = "일본";
        City tokyo = createCity("도쿄", "Tokyo");
        City osaka = createCity("오사카", "Osaka");

        given(cityRepository.searchByKeyword(anyString(), any(Pageable.class)))
                .willReturn(List.of(tokyo, osaka));

        // when
        SearchResult result = searchService.search(keyword);

        // then
        assertThat(result).isInstanceOf(CitySearchResult.class);
        CitySearchResult cityResult = (CitySearchResult) result;
        assertThat(cityResult.cities()).hasSize(2);
    }

    @DisplayName("여러 장소를 검색할 수 있다")
    @Test
    void searchMultiplePlaces() {
        // given
        String keyword = "에펠탑 기념품";
        SearchPlace place1 = createPlace("에펠탑 기념품샵 A", "프랑스 파리");
        SearchPlace place2 = createPlace("에펠탑 기념품샵 B", "프랑스 파리");

        given(cityRepository.searchByKeyword(anyString(), any(Pageable.class)))
                .willReturn(List.of());
        given(placeSearchProvider.searchByKeyword(keyword))
                .willReturn(List.of(place1, place2));

        // when
        SearchResult result = searchService.search(keyword);

        // then
        assertThat(result).isInstanceOf(PlaceSearchResult.class);
        PlaceSearchResult placeResult = (PlaceSearchResult) result;
        assertThat(placeResult.places()).hasSize(2);
    }

    private City createCity(String nameKr, String nameEn) {
        Country country = createCountry();

        return City.create(
                nameEn,
                nameKr,
                BigDecimal.valueOf(35.6762),
                BigDecimal.valueOf(139.6503),
                country
        );
    }

    private Country createCountry() {
        return Country.of(
                "Japan",
                "일본",
                "JP",
                "Tokyo",
                Region.ASIA,
                "https://example.com/japan.png",
                BigDecimal.valueOf(35.6762),
                BigDecimal.valueOf(139.6503),
                null
        );
    }

    private SearchPlace createPlace(String name, String address) {
        return new SearchPlace(
                name,
                address,
                null,
                null,
                Coordinate.of(
                        BigDecimal.valueOf(48.8584),
                        BigDecimal.valueOf(2.2945)
                )
        );
    }
}
