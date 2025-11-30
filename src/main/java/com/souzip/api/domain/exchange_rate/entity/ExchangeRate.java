package com.souzip.api.domain.exchange_rate.entity;

import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "exchange_rates")
@Entity
public class ExchangeRate extends BaseEntity {

    @Column(nullable = false)
    private String baseCode;

    @Column(nullable = false)
    private String currencyCode;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal rate;

    public static ExchangeRate of(
        String baseCode,
        String currencyCode,
        BigDecimal rate
    ) {
        return ExchangeRate.builder()
            .baseCode(baseCode)
            .currencyCode(currencyCode)
            .rate(rate)
            .build();
    }
}
