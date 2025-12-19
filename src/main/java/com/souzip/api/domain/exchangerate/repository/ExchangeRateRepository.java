package com.souzip.api.domain.exchangerate.repository;

import com.souzip.api.domain.exchangerate.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findByCurrencyCodeAndBaseCode(String currencyCode, String baseCode);
}
