package com.souzip.api.domain.product.entity;

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
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column
    private String imageUrl;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;

    @Column(nullable = false)
    private String location;

    @Column
    private String address;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    public static Product of(
            String name,
            Integer price,
            String imageUrl,
            String description,
            Category category,
            Purpose purpose,
            String location,
            String address
    ) {
        return Product.builder()
                .name(name)
                .price(price)
                .imageUrl(imageUrl)
                .description(description)
                .category(category)
                .purpose(purpose)
                .location(location)
                .address(address)
                .deleted(false)
                .build();
    }

    public void update(String name, Integer price, String imageUrl, String description,
                       Category category, Purpose purpose, String location, String address) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.category = category;
        this.purpose = purpose;
        this.location = location;
        this.address = address;
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }
}
