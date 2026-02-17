package com.souzip.api.domain.city.application.query;

import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CityQueryService {

    private final CityRepository cityRepository;

    public List<CityQueryResult> getCities(Long countryId) {
        return cityRepository.findByCountryId(countryId).stream()
            .map(CityQueryResult::from)
            .toList();
    }

    public record CityQueryResult(
        Long id,
        String nameKr,
        String nameEn,
        Integer priority
    ) {
        public static CityQueryResult from(City city) {
            return new CityQueryResult(
                city.getId(),
                city.getNameKr(),
                city.getNameEn(),
                city.getPriority()
            );
        }
    }
}
