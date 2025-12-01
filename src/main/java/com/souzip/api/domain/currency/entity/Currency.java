package com.souzip.api.domain.currency.entity;

import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Currency extends BaseEntity {

    @Column(unique = true, nullable = false, length = 10)
    private String code;

    @Column(length = 10)
    private String symbol;

    public static Currency of(String code, String symbol) {
        return Currency.builder()
            .code(code)
            .symbol(symbol)
            .build();
    }
}
