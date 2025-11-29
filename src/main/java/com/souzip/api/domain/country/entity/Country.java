package com.souzip.api.domain.country.entity;

import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "countries")
@Entity
public class Country extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    private String capital;

    @Column(nullable = false)
    private String region;

    private String flags;
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    public Object getCode() {
        return code;
    }

    public static Country of(
        String name,
        String code,
        String capital,
        String region,
        String flags,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        return Country.builder()
            .name(name)
            .code(code)
            .capital(capital)
            .region(region)
            .flags(flags)
            .latitude(latitude)
            .longitude(longitude)
            .build();
    }

}
