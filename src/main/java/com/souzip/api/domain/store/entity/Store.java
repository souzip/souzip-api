package com.souzip.api.domain.store.entity;

import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Store extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private City city;

    @Column(nullable = false, precision = 10, scale = 6)
    private Double latitude;

    @Column(nullable = false, precision = 10, scale = 6)
    private Double longitude;

    @Column(length = 255)
    private String note;

    public static Store of(
        Country country,
        City city,
        Double latitude,
        Double longitude,
        String note
    ) {
        return Store.builder()
            .country(country)
            .city(city)
            .latitude(latitude)
            .longitude(longitude)
            .note(note)
            .build();
    }
}
