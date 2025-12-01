package com.souzip.api.domain.exchange_rate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "exchange_rates")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

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
