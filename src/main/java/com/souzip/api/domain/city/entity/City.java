package com.souzip.api.domain.city.entity;

import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "city", indexes = {
    @Index(name = "idx_city_country", columnList = "country_id"),
    @Index(name = "idx_city_name", columnList = "name_en, name_kr"),
    @Index(name = "idx_city_priority", columnList = "country_id, priority")
})
public class City extends BaseEntity {

    @Column(nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private String nameKr;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Column(nullable = true)
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Country country;

    public static City of(
        String nameEn,
        String nameKr,
        BigDecimal latitude,
        BigDecimal longitude,
        Country country
    ) {
        return City.builder()
            .nameEn(nameEn)
            .nameKr(nameKr)
            .latitude(latitude)
            .longitude(longitude)
            .country(country)
            .priority(null)
            .build();
    }
}
