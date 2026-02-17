package com.souzip.api.domain.city.application.command;

import com.souzip.api.domain.admin.event.AdminCityPriorityChangeRequestedEvent;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.search.scheduler.SearchIndexScheduler;
import com.souzip.api.global.exception.BusinessException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CityCommandServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private SearchIndexScheduler searchIndexScheduler;

    @InjectMocks
    private CityCommandService cityCommandService;

    @DisplayName("도시 우선순위 설정 성공 - 기존 우선순위 없음")
    @Test
    void handlePriorityChangeRequested_withNoPreviousPriority_success() {
        // given
        Long cityId = 1L;
        Integer newPriority = 1;

        Country country = mock(Country.class);
        given(country.getId()).willReturn(1L);

        City city = City.of("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));

        AdminCityPriorityChangeRequestedEvent event =
            AdminCityPriorityChangeRequestedEvent.of(cityId, newPriority);

        // when
        cityCommandService.handlePriorityChangeRequested(event);

        // then
        verify(cityRepository).findByIdWithLock(cityId);
        verify(cityRepository, never()).pullPriorityFrom(any(), any());
        verify(cityRepository).shiftPriorityFrom(newPriority, 1L);
        verify(searchIndexScheduler).markReindexNeeded();
        assertThat(city.getPriority()).isEqualTo(newPriority);
    }

    @DisplayName("도시 우선순위 설정 성공 - 기존 우선순위 있음")
    @Test
    void handlePriorityChangeRequested_withPreviousPriority_success() {
        // given
        Long cityId = 1L;
        Integer newPriority = 3;

        Country country = mock(Country.class);
        given(country.getId()).willReturn(1L);

        City city = City.of("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);
        city.updatePriority(1);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));

        AdminCityPriorityChangeRequestedEvent event =
            AdminCityPriorityChangeRequestedEvent.of(cityId, newPriority);

        // when
        cityCommandService.handlePriorityChangeRequested(event);

        // then
        verify(cityRepository).pullPriorityFrom(1, 1L);
        verify(cityRepository).shiftPriorityFrom(newPriority, 1L);
        verify(searchIndexScheduler).markReindexNeeded();
        assertThat(city.getPriority()).isEqualTo(newPriority);
    }

    @DisplayName("도시 우선순위 초기화 성공 - null로 설정")
    @Test
    void handlePriorityChangeRequested_resetToNull_success() {
        // given
        Long cityId = 1L;

        Country country = mock(Country.class);
        given(country.getId()).willReturn(1L);

        City city = City.of("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);
        city.updatePriority(1);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));

        AdminCityPriorityChangeRequestedEvent event =
            AdminCityPriorityChangeRequestedEvent.of(cityId, null);

        // when
        cityCommandService.handlePriorityChangeRequested(event);

        // then
        verify(cityRepository).pullPriorityFrom(1, 1L);
        verify(cityRepository, never()).shiftPriorityFrom(any(), any());
        verify(searchIndexScheduler).markReindexNeeded();
        assertThat(city.getPriority()).isNull();
    }

    @DisplayName("존재하지 않는 도시 우선순위 설정 시 예외 발생")
    @Test
    void handlePriorityChangeRequested_cityNotFound_throwsException() {
        // given
        Long cityId = 999L;

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.empty());

        AdminCityPriorityChangeRequestedEvent event =
            AdminCityPriorityChangeRequestedEvent.of(cityId, 1);

        // when & then
        assertThatThrownBy(() -> cityCommandService.handlePriorityChangeRequested(event))
            .isInstanceOf(BusinessException.class);

        verify(cityRepository, never()).pullPriorityFrom(any(), any());
        verify(cityRepository, never()).shiftPriorityFrom(any(), any());
        verify(searchIndexScheduler, never()).markReindexNeeded();
    }
}
