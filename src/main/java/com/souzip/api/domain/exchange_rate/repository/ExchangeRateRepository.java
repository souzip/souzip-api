package com.souzip.api.domain.exchange_rate.repository;

import com.souzip.api.domain.exchange_rate.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByBaseCodeAndCurrencyCode(String baseCode, String currencyCode);
}
