package com.souzip.api.domain.country.entity;

import com.souzip.api.domain.currency.entity.Currency;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Country extends BaseEntity {

    @Column(nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private String nameKr;

    @Column(nullable = false)
    private String code;

    @Column
    private String capital;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @ManyToOne
    @JoinColumn(name = "currency_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Currency currency;

    public static Country of(
        String nameEn,
        String nameKr,
        String code,
        String capital,
        Region region,
        String imageUrl,
        BigDecimal latitude,
        BigDecimal longitude,
        Currency currency
    ) {
        return Country.builder()
            .nameEn(nameEn)
            .nameKr(nameKr)
            .code(code)
            .capital(capital)
            .region(region)
            .imageUrl(imageUrl)
            .latitude(latitude)
            .longitude(longitude)
            .currency(currency)
            .build();
    }
}
