package com.souzip.domain.city.entity;

import com.souzip.domain.country.entity.Country;
import com.souzip.domain.shared.BaseEntity;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
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

    public static City create(
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

    public void updatePriority(Integer priority) {
        validatePriority(priority);
        this.priority = priority;
    }

    public void updateName(String nameEn, String nameKr) {
        this.nameEn = nameEn;
        this.nameKr = nameKr;
    }

    private void validatePriority(Integer priority) {
        if (isInvalidPriority(priority)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "우선순위는 1 이상이어야 합니다.");
        }
    }

    private boolean isInvalidPriority(Integer priority) {
        return priority != null && priority < 1;
    }
}
