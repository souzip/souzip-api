package com.souzip.api.domain.city.repository;

import com.souzip.api.domain.city.entity.City;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CityRepositoryCustom {

    Optional<City> findByIdWithLock(Long cityId);

    List<City> findByCountryId(Long countryId);

    void shiftPriorityFrom(Integer priority, Long countryId);

    void pullPriorityFrom(Integer priority, Long countryId);

    Page<City> findByCountryIdWithPaging(Long countryId, Pageable pageable);

    Page<City> searchByKeyword(Long countryId, String keyword, Pageable pageable);

}
