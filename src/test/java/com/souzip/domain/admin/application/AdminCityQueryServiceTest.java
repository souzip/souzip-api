package com.souzip.domain.admin.application;

import com.souzip.domain.admin.application.port.CityQueryPort;
import com.souzip.domain.admin.application.port.CityQueryPort.CityQueryResult;
import com.souzip.domain.admin.application.query.AdminCityQueryService;
import com.souzip.domain.admin.application.query.CitySearchQuery;
import com.souzip.shared.common.dto.pagination.PaginationResponse;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminCityQueryServiceTest {

    @Mock
    private CityQueryPort cityQueryPort;

    @InjectMocks
    private AdminCityQueryService adminCityQueryService;

    @DisplayName("키워드 없이 도시 목록 페이징 조회 성공")
    @Test
    void getCities_withoutKeyword_success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        CitySearchQuery query = CitySearchQuery.of(83L, null, 1, 20);

        List<CityQueryResult> content = List.of(
                new CityQueryResult(1L, "서울", "Seoul", 1, now),
                new CityQueryResult(2L, "부산", "Busan", 2, now)
        );

        PaginationResponse<CityQueryResult> expected = PaginationResponse.of(
                content, 1, 20, 2, 1
        );

        given(cityQueryPort.getCities(83L, null, 1, 20)).willReturn(expected);

        // when
        PaginationResponse<CityQueryResult> result = adminCityQueryService.getCities(query);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).nameKr()).isEqualTo("서울");
        assertThat(result.getContent().get(0).nameEn()).isEqualTo("Seoul");
        assertThat(result.getContent().get(1).nameKr()).isEqualTo("부산");
        assertThat(result.getContent().get(1).nameEn()).isEqualTo("Busan");
        assertThat(result.getPagination().getTotalItems()).isEqualTo(2);

        verify(cityQueryPort).getCities(83L, null, 1, 20);
    }

    @DisplayName("키워드로 도시 검색 페이징 조회 성공")
    @Test
    void getCities_withKeyword_success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        CitySearchQuery query = CitySearchQuery.of(83L, "서울", 1, 20);

        List<CityQueryResult> content = List.of(
                new CityQueryResult(1L, "서울", "Seoul", 1, now)
        );

        PaginationResponse<CityQueryResult> expected = PaginationResponse.of(
                content, 1, 20, 1, 1
        );

        given(cityQueryPort.getCities(83L, "서울", 1, 20)).willReturn(expected);

        // when
        PaginationResponse<CityQueryResult> result = adminCityQueryService.getCities(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().nameKr()).isEqualTo("서울");
        assertThat(result.getContent().getFirst().nameEn()).isEqualTo("Seoul");

        verify(cityQueryPort).getCities(83L, "서울", 1, 20);
    }
}
