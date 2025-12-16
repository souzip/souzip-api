package com.souzip.api.domain.souvenir.entity;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Souvenir extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;

    @Column(nullable = false)
    private Long cityId;

    @Column(nullable = false)
    private Long userId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    public static Souvenir of(
            String name,
            Integer price,
            String description,
            Category category,
            Purpose purpose,
            Long cityId,
            Long userId
    ) {
        return Souvenir.builder()
                .name(name)
                .price(price)
                .description(description)
                .category(category)
                .purpose(purpose)
                .cityId(cityId)
                .userId(userId)
                .deleted(false)
                .build();
    }

    public void update(String name, Integer price, String description,
                       Category category, Purpose purpose, Long cityId) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.purpose = purpose;
        this.cityId = cityId;
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }
}
