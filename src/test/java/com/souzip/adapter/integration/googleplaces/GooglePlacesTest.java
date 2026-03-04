package com.souzip.adapter.integration.googleplaces;

import com.souzip.adapter.integration.googleplaces.dto.GooglePlacesSearchResponse;
import com.souzip.application.location.dto.SearchPlace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GooglePlacesTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GooglePlaces adapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "baseUrl", "https://maps.googleapis.com/maps/api/place/textsearch/json");
        ReflectionTestUtils.setField(adapter, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(adapter, "language", "ko");
    }

    @DisplayName("키워드로 장소를 검색할 수 있다")
    @Test
    void searchByKeyword_Success() {
        // given
        String keyword = "파리 에펠탑 기념품";
        GooglePlacesSearchResponse mockResponse = createMockResponse();

        when(restTemplate.getForObject(anyString(), eq(GooglePlacesSearchResponse.class)))
                .thenReturn(mockResponse);

        // when
        List<SearchPlace> places = adapter.searchByKeyword(keyword);

        // then
        assertThat(places).hasSize(1);
        assertThat(places.getFirst().name()).isEqualTo("에펠탑 기념품샵");
        assertThat(places.getFirst().address()).isEqualTo("프랑스 파리 샹드마르스 에펠탑");
        assertThat(places.getFirst().region()).isEqualTo("프랑스 파리 1구");
        assertThat(places.getFirst().category()).isEqualTo("store");
        assertThat(places.getFirst().coordinate().getLatitude()).isEqualTo(BigDecimal.valueOf(48.8584));
        assertThat(places.getFirst().coordinate().getLongitude()).isEqualTo(BigDecimal.valueOf(2.2945));
    }

    @DisplayName("types가 null이면 category는 null로 반환한다")
    @Test
    void searchByKeyword_NullTypes() {
        // given
        GooglePlacesSearchResponse mockResponse = createMockResponseWithNullTypes();

        when(restTemplate.getForObject(anyString(), eq(GooglePlacesSearchResponse.class)))
                .thenReturn(mockResponse);

        // when
        List<SearchPlace> places = adapter.searchByKeyword("test");

        // then
        assertThat(places.getFirst().category()).isNull();
    }

    @DisplayName("plus_code가 null이면 region은 null로 반환한다")
    @Test
    void searchByKeyword_NullPlusCode() {
        // given
        GooglePlacesSearchResponse mockResponse = createMockResponseWithNullPlusCode();

        when(restTemplate.getForObject(anyString(), eq(GooglePlacesSearchResponse.class)))
                .thenReturn(mockResponse);

        // when
        List<SearchPlace> places = adapter.searchByKeyword("test");

        // then
        assertThat(places.getFirst().region()).isNull();
    }

    @DisplayName("상위 10개만 반환한다")
    @Test
    void searchByKeyword_LimitTo10() {
        // given
        String keyword = "강남역 카페";
        GooglePlacesSearchResponse mockResponse = createMockResponseWith20Results();

        when(restTemplate.getForObject(anyString(), eq(GooglePlacesSearchResponse.class)))
                .thenReturn(mockResponse);

        // when
        List<SearchPlace> places = adapter.searchByKeyword(keyword);

        // then
        assertThat(places).hasSize(10);
    }

    @DisplayName("응답이 null이면 빈 리스트를 반환한다")
    @Test
    void searchByKeyword_NullResponse() {
        // given
        when(restTemplate.getForObject(anyString(), eq(GooglePlacesSearchResponse.class)))
                .thenReturn(null);

        // when
        List<SearchPlace> places = adapter.searchByKeyword("test");

        // then
        assertThat(places).isEmpty();
    }

    @DisplayName("결과가 비어있으면 빈 리스트를 반환한다")
    @Test
    void searchByKeyword_EmptyResults() {
        // given
        GooglePlacesSearchResponse emptyResponse = new GooglePlacesSearchResponse(List.of(), "ZERO_RESULTS");

        when(restTemplate.getForObject(anyString(), eq(GooglePlacesSearchResponse.class)))
                .thenReturn(emptyResponse);

        // when
        List<SearchPlace> places = adapter.searchByKeyword("존재하지않는장소12345");

        // then
        assertThat(places).isEmpty();
    }

    @DisplayName("API 호출 실패 시 빈 리스트를 반환한다")
    @Test
    void searchByKeyword_ApiCallFails() {
        // given
        when(restTemplate.getForObject(anyString(), eq(GooglePlacesSearchResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        // when
        List<SearchPlace> places = adapter.searchByKeyword("test");

        // then
        assertThat(places).isEmpty();
    }

    private GooglePlacesSearchResponse createMockResponse() {
        GooglePlacesSearchResponse.Location location =
                new GooglePlacesSearchResponse.Location(48.8584, 2.2945);
        GooglePlacesSearchResponse.Geometry geometry =
                new GooglePlacesSearchResponse.Geometry(location);
        GooglePlacesSearchResponse.PlusCode plusCode =
                new GooglePlacesSearchResponse.PlusCode("8FW4V75Q+GH 프랑스 파리 1구");
        GooglePlacesSearchResponse.Result result =
                new GooglePlacesSearchResponse.Result(
                        "에펠탑 기념품샵",
                        "프랑스 파리 샹드마르스 에펠탑",
                        geometry,
                        List.of("store", "point_of_interest", "establishment"),
                        plusCode
                );

        return new GooglePlacesSearchResponse(List.of(result), "OK");
    }

    private GooglePlacesSearchResponse createMockResponseWithNullTypes() {
        GooglePlacesSearchResponse.Location location =
                new GooglePlacesSearchResponse.Location(48.8584, 2.2945);
        GooglePlacesSearchResponse.Geometry geometry =
                new GooglePlacesSearchResponse.Geometry(location);
        GooglePlacesSearchResponse.PlusCode plusCode =
                new GooglePlacesSearchResponse.PlusCode("8FW4V75Q+GH 프랑스 파리 1구");
        GooglePlacesSearchResponse.Result result =
                new GooglePlacesSearchResponse.Result(
                        "테스트 장소",
                        "테스트 주소",
                        geometry,
                        null,
                        plusCode
                );

        return new GooglePlacesSearchResponse(List.of(result), "OK");
    }

    private GooglePlacesSearchResponse createMockResponseWithNullPlusCode() {
        GooglePlacesSearchResponse.Location location =
                new GooglePlacesSearchResponse.Location(48.8584, 2.2945);
        GooglePlacesSearchResponse.Geometry geometry =
                new GooglePlacesSearchResponse.Geometry(location);
        GooglePlacesSearchResponse.Result result =
                new GooglePlacesSearchResponse.Result(
                        "테스트 장소",
                        "테스트 주소",
                        geometry,
                        List.of("cafe"),
                        null
                );

        return new GooglePlacesSearchResponse(List.of(result), "OK");
    }

    private GooglePlacesSearchResponse createMockResponseWith20Results() {
        List<GooglePlacesSearchResponse.Result> results = IntStream.range(0, 20)
                .mapToObj(i -> {
                    GooglePlacesSearchResponse.Location location =
                            new GooglePlacesSearchResponse.Location(37.498 + i * 0.001, 127.028 + i * 0.001);
                    GooglePlacesSearchResponse.Geometry geometry =
                            new GooglePlacesSearchResponse.Geometry(location);
                    GooglePlacesSearchResponse.PlusCode plusCode =
                            new GooglePlacesSearchResponse.PlusCode("GG3V+" + i + " 서울특별시 강남구");

                    return new GooglePlacesSearchResponse.Result(
                            "카페 " + (i + 1),
                            "서울 강남구 테헤란로 " + (i + 1),
                            geometry,
                            List.of("cafe", "establishment"),
                            plusCode
                    );
                })
                .toList();

        return new GooglePlacesSearchResponse(results, "OK");
    }
}
