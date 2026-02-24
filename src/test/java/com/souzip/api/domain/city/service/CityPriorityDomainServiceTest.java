package com.souzip.api.domain.city.service;

import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityPriorityDomainServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityPriorityDomainService cityPriorityDomainService;

    @DisplayName("같은 우선순위로 변경 시 아무것도 실행되지 않는다")
    @Test
    void adjustPriorities_SamePriority_DoesNothing() {
        // given
        Long excludeCityId = 1L;
        Integer oldPriority = 3;
        Integer newPriority = 3;
        Long countryId = 1L;

        // when
        cityPriorityDomainService.adjustPriorities(excludeCityId, oldPriority, newPriority, countryId);

        // then
        verify(cityRepository, never()).findByCountryIdAndPriorityGoeOrderByPriorityAsc(anyLong(), anyInt());
    }

    @DisplayName("우선순위 없음 -> 3번으로 설정 시 3번 이후가 밀린다")
    @Test
    void adjustPriorities_NullToNumber_PushesExisting() {
        // given
        Long excludeCityId = 1L;
        Integer oldPriority = null;
        Integer newPriority = 3;
        Long countryId = 1L;

        City city3 = mock(City.class);
        City city4 = mock(City.class);
        City city5 = mock(City.class);

        when(city3.getId()).thenReturn(3L);
        when(city4.getId()).thenReturn(4L);
        when(city5.getId()).thenReturn(5L);

        when(city3.getPriority()).thenReturn(3);
        when(city4.getPriority()).thenReturn(4);
        when(city5.getPriority()).thenReturn(5);

        when(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 3))
                .thenReturn(Arrays.asList(city3, city4, city5));

        // when
        cityPriorityDomainService.adjustPriorities(excludeCityId, oldPriority, newPriority, countryId);

        // then
        verify(cityRepository, times(1)).findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 3);
        verify(city3).updatePriority(4);
        verify(city4).updatePriority(5);
        verify(city5).updatePriority(6);
    }

    @DisplayName("3번 우선순위 제거 시 4번 이후가 당겨진다")
    @Test
    void adjustPriorities_NumberToNull_PullsFollowing() {
        // given
        Long excludeCityId = 3L;
        Integer oldPriority = 3;
        Integer newPriority = null;
        Long countryId = 1L;

        City city4 = mock(City.class);
        City city5 = mock(City.class);

        when(city4.getId()).thenReturn(4L);
        when(city5.getId()).thenReturn(5L);

        when(city4.getPriority()).thenReturn(4);
        when(city5.getPriority()).thenReturn(5);

        when(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 4))
                .thenReturn(Arrays.asList(city4, city5));

        // when
        cityPriorityDomainService.adjustPriorities(excludeCityId, oldPriority, newPriority, countryId);

        // then
        verify(cityRepository, times(1)).findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 4);
        verify(city4).updatePriority(3);
        verify(city5).updatePriority(4);
    }

    @DisplayName("2번 -> 5번으로 변경 시 올바르게 조정된다")
    @Test
    void adjustPriorities_LowerToHigher_AdjustsCorrectly() {
        // given
        Long excludeCityId = 2L;
        Integer oldPriority = 2;
        Integer newPriority = 5;
        Long countryId = 1L;

        City city3 = mock(City.class);
        City city4 = mock(City.class);
        City city5 = mock(City.class);

        when(city3.getId()).thenReturn(3L);
        when(city4.getId()).thenReturn(4L);
        when(city5.getId()).thenReturn(5L);

        when(city3.getPriority()).thenReturn(3);
        when(city4.getPriority()).thenReturn(4);
        when(city5.getPriority()).thenReturn(5);

        when(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 3))
                .thenReturn(Arrays.asList(city3, city4, city5));

        when(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 4))
                .thenReturn(Collections.emptyList());

        // when
        cityPriorityDomainService.adjustPriorities(excludeCityId, oldPriority, newPriority, countryId);

        // then
        verify(cityRepository, times(1)).findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 3);
        verify(cityRepository, times(1)).findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 4);
    }

    @DisplayName("5번 -> 2번으로 변경 시 올바르게 조정된다")
    @Test
    void adjustPriorities_HigherToLower_AdjustsCorrectly() {
        // given
        Long excludeCityId = 5L;
        Integer oldPriority = 5;
        Integer newPriority = 2;
        Long countryId = 1L;

        City city2 = mock(City.class);
        City city3 = mock(City.class);
        City city4 = mock(City.class);

        when(city2.getId()).thenReturn(2L);
        when(city3.getId()).thenReturn(3L);
        when(city4.getId()).thenReturn(4L);

        when(city2.getPriority()).thenReturn(2);
        when(city3.getPriority()).thenReturn(3);
        when(city4.getPriority()).thenReturn(4);

        when(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 2))
                .thenReturn(Arrays.asList(city2, city3, city4));

        when(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 6))
                .thenReturn(Collections.emptyList());

        // when
        cityPriorityDomainService.adjustPriorities(excludeCityId, oldPriority, newPriority, countryId);

        // then
        verify(cityRepository, times(1)).findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 2);
        verify(cityRepository, times(1)).findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 6);
    }

    @DisplayName("연속되지 않은 우선순위가 있을 때 중간에서 멈춘다")
    @Test
    void adjustPriorities_NonConsecutivePriorities_StopsAtGap() {
        // given
        Long excludeCityId = 2L;
        Integer oldPriority = 2;
        Integer newPriority = null;
        Long countryId = 1L;

        City city3 = mock(City.class);
        City city5 = mock(City.class);

        when(city3.getId()).thenReturn(3L);
        when(city5.getId()).thenReturn(5L);

        when(city3.getPriority()).thenReturn(3);
        when(city5.getPriority()).thenReturn(5);

        when(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 3))
                .thenReturn(Arrays.asList(city3, city5));

        // when
        cityPriorityDomainService.adjustPriorities(excludeCityId, oldPriority, newPriority, countryId);

        // then
        verify(cityRepository, times(1)).findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, 3);
        verify(city3).updatePriority(2);
        verify(city5, never()).updatePriority(anyInt());
    }
}
