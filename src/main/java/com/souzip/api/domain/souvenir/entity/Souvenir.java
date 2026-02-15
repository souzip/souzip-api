package com.souzip.api.domain.souvenir.entity;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.souvenir.dto.SouvenirCreateRequest;
import com.souzip.api.domain.souvenir.dto.SouvenirUpdateRequest;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String locationDetail;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @Column
    private String countryCode;

    public static Souvenir of(
        SouvenirCreateRequest request,
        User user,
        Integer calculatedLocalPrice,
        Integer calculatedKrwPrice
    ) {
        return Souvenir.builder()
            .name(request.name())
            .localPrice(calculatedLocalPrice)
            .currencySymbol(request.currencySymbol())
            .krwPrice(calculatedKrwPrice)
            .description(request.description())
            .address(request.address())
            .locationDetail(request.locationDetail())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .category(request.category())
            .purpose(request.purpose())
            .countryCode(request.countryCode())
            .user(user)
            .deleted(false)
            .build();
    }

    public void update(
        SouvenirUpdateRequest request,
        Integer calculatedLocalPrice,
        Integer calculatedKrwPrice
    ) {
        this.name = request.name();
        this.localPrice = calculatedLocalPrice;
        this.currencySymbol = request.currencySymbol();
        this.krwPrice = calculatedKrwPrice;
        this.description = request.description();
        this.address = request.address();
        this.locationDetail = request.locationDetail();
        this.latitude = request.latitude();
        this.longitude = request.longitude();
        this.category = request.category();
        this.purpose = request.purpose();
        this.countryCode = request.countryCode();
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isOwnedBy(String userId) {
        return this.user.getUserId().equals(userId);
    }
}
