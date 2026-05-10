package com.souzip.domain.country.entity;

import com.souzip.domain.currency.entity.Currency;
import com.souzip.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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
