package com.souzip.api.domain.country.entity;

import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "countries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Country extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    private String capital;

    @Column(nullable = false)
    private String region;

    private String flags;
    private Double latitude;
    private Double longitude;

    public static Country of(
        String name,
        String code,
        String capital,
        String region,
        String flags,
        Double latitude,
        Double longitude
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
