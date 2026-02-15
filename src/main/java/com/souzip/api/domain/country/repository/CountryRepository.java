package com.souzip.api.domain.country.repository;

import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByCode(String code);

    @Query("SELECT c FROM Country c JOIN FETCH c.currency WHERE c.code = :code")
    Optional<Country> findByCodeWithCurrency(@Param("code") String code);
}
