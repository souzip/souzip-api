package com.souzip.api.domain.country.repository;

import com.souzip.api.domain.country.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByCode(String code);

    List<Country> findByRegion(String region);

    List<Country> findByNameContaining(String name);

    long countByRegion(String region);
}
