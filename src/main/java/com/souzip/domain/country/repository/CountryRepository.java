package com.souzip.domain.country.repository;

import com.souzip.domain.country.entity.Country;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByCode(String code);

    @Query("SELECT c FROM Country c JOIN FETCH c.currency WHERE c.code = :code")
    Optional<Country> findByCodeWithCurrency(@Param("code") String code);

    List<Country> findAllByOrderByNameKrAsc();

    @Query("SELECT c FROM Country c WHERE " +
            "c.nameKr LIKE %:keyword% OR c.nameEn LIKE %:keyword% " +
            "ORDER BY c.nameKr ASC")
    List<Country> findByKeywordOrderByNameKrAsc(@Param("keyword") String keyword);
}
