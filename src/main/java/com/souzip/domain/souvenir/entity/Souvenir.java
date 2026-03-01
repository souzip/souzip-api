package com.souzip.domain.souvenir.entity;

import com.souzip.domain.category.entity.Category;
import com.souzip.domain.souvenir.dto.SouvenirCreateRequest;
import com.souzip.domain.souvenir.dto.SouvenirRequest;
import com.souzip.domain.souvenir.dto.SouvenirUpdateRequest;
import com.souzip.domain.souvenir.vo.PriceInfo;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.shared.BaseEntity;
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

    @Deprecated
    @Column
    private Integer localPrice;

    @Deprecated
    @Column(length = 10)
    private String currencySymbol;

    @Deprecated
    @Column
    private Integer krwPrice;

    private Integer originalAmount;

    private String originalCurrency;

    private Integer exchangeAmount;

    private Integer convertedAmount;

    private String convertedCurrency;

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

    @Deprecated
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

    public static Souvenir ofV2(
        SouvenirRequest request,
        User user,
        PriceInfo originalPrice,
        Integer exchangeAmount,
        String currencySymbol,
        PriceInfo convertedPrice
    ) {
        return Souvenir.builder()
            .name(request.name())
            .originalAmount(extractAmount(originalPrice))
            .originalCurrency(extractCurrency(originalPrice))
            .exchangeAmount(exchangeAmount)
            .convertedAmount(extractAmount(convertedPrice))
            .convertedCurrency(extractCurrency(convertedPrice))
            .localPrice(extractAmount(originalPrice))
            .currencySymbol(currencySymbol)
            .krwPrice(exchangeAmount)
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

    @Deprecated
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

    public void updateV2(
        SouvenirRequest request,
        PriceInfo originalPrice,
        Integer exchangeAmount,
        String currencySymbol,
        PriceInfo convertedPrice
    ) {
        this.name = request.name();
        this.originalAmount = extractAmount(originalPrice);
        this.originalCurrency = extractCurrency(originalPrice);
        this.exchangeAmount = exchangeAmount;
        this.convertedAmount = extractAmount(convertedPrice);
        this.convertedCurrency = extractCurrency(convertedPrice);
        this.localPrice = extractAmount(originalPrice);
        this.currencySymbol = currencySymbol;
        this.krwPrice = exchangeAmount;
        this.description = request.description();
        this.address = request.address();
        this.locationDetail = request.locationDetail();
        this.latitude = request.latitude();
        this.longitude = request.longitude();
        this.category = request.category();
        this.purpose = request.purpose();
        this.countryCode = request.countryCode();
    }

    public PriceInfo getOriginalPrice() {
        if (hasNoOriginalPrice()) {
            return null;
        }
        return PriceInfo.of(originalAmount, originalCurrency);
    }

    public PriceInfo getConvertedPrice() {
        if (hasNoConvertedPrice()) {
            return null;
        }
        return PriceInfo.of(convertedAmount, convertedCurrency);
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isOwnedBy(String userId) {
        return this.user.getUserId().equals(userId);
    }

    private static Integer extractAmount(PriceInfo priceInfo) {
        if (priceInfo == null) {
            return null;
        }
        return priceInfo.getAmount();
    }

    private static String extractCurrency(PriceInfo priceInfo) {
        if (priceInfo == null) {
            return null;
        }
        return priceInfo.getCurrency();
    }

    private boolean hasNoOriginalPrice() {
        return originalAmount == null || originalCurrency == null;
    }

    private boolean hasNoConvertedPrice() {
        return convertedAmount == null || convertedCurrency == null;
    }
}
