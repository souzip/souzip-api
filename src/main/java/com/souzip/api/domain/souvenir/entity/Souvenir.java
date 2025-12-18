package com.souzip.api.domain.souvenir.entity;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Souvenir extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String name;

    @Column
    private Integer localPrice;

    @Column(length = 10)
    private String currencySymbol;

    @Column
    private Integer krwPrice;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 255)
    private String locationDetail;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @Column
    private String countryCode;

    public static Souvenir of(
            String name,
            Integer localPrice,
            String currencySymbol,
            Integer krwPrice,
            String description,
            String address,
            String locationDetail,
            BigDecimal latitude,
            BigDecimal longitude,
            Category category,
            Purpose purpose,
            String countryCode,
            Long userId
    ) {
        return Souvenir.builder()
                .name(name)
                .localPrice(localPrice)
                .currencySymbol(currencySymbol)
                .krwPrice(krwPrice)
                .description(description)
                .address(address)
                .locationDetail(locationDetail)
                .latitude(latitude)
                .longitude(longitude)
                .category(category)
                .purpose(purpose)
                .countryCode(countryCode)
                .userId(userId)
                .deleted(false)
                .build();
    }

    public void update(
            String name,
            Integer localPrice,
            String currencySymbol,
            Integer krwPrice,
            String description,
            String address,
            String locationDetail,
            BigDecimal latitude,
            BigDecimal longitude,
            Category category,
            Purpose purpose,
            String countryCode
    ) {
        this.name = name;
        this.localPrice = localPrice;
        this.currencySymbol = currencySymbol;
        this.krwPrice = krwPrice;
        this.description = description;
        this.address = address;
        this.locationDetail = locationDetail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.purpose = purpose;
        this.countryCode = countryCode;
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }
}
