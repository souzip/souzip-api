package com.souzip.api.domain.currency.repository;

import com.souzip.api.domain.currency.entity.Currency;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    Optional<Currency> findByCode(String code);

    Optional<Currency> findBySymbol(String symbol);
}
