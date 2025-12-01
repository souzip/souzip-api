package com.souzip.api.domain.country.repository;

import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long>, CountryRepositoryCustom {

    Optional<Country> findByCode(String code);

    List<Country> findByRegion(Region region);

    long countByRegion(Region region);
}
