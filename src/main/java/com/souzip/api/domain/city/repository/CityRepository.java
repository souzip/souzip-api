package com.souzip.api.domain.city.repository;

import com.souzip.api.domain.city.entity.City;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long>, CityRepositoryCustom {

    Optional<City> findById(Long id);
}
