package com.souzip.api.domain.souvenir.entity;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.souvenir.dto.SouvenirCreateRequest;
import com.souzip.api.domain.souvenir.dto.SouvenirRequest;
import com.souzip.api.domain.souvenir.dto.SouvenirUpdateRequest;
import com.souzip.api.domain.souvenir.vo.PriceInfo;
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

    // ==================== 기존 필드 (하위 호환) ====================

    /**
     * @deprecated v2 API에서는 originalAmount 사용. 향후 버전에서 제거 예정.
     */
    @Deprecated
    @Column
    private Integer localPrice;

    /**
     * @deprecated v2 API에서는 originalCurrency 기반 심볼 조회. 향후 버전에서 제거 예정.
     */
    @Deprecated
    @Column(length = 10)
    private String currencySymbol;

    /**
     * @deprecated v2 API에서는 exchangeAmount 사용. 향후 버전에서 제거 예정.
     */
    @Deprecated
    @Column
    private Integer krwPrice;

    // ==================== 신규 필드 (v2) ====================

    /**
     * 사용자가 입력한 원본 금액
     */
    @Column(name = "original_amount")
    private Integer originalAmount;

    /**
     * 사용자가 입력한 원본 통화 (ISO 4217 코드)
     */
    @Column(name = "original_currency", length = 3)
    private String originalCurrency;

    /**
     * 원화로 환산된 금액 (검색/정렬용)
     */
    @Column(name = "exchange_amount")
    private Integer exchangeAmount;

    // ==================== 공통 필드 ====================

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

    // ==================== 생성 메서드 (v1 - Deprecated) ====================

    /**
     * @deprecated v2 API에서는 ofV2() 사용 권장
     */
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

    // ==================== 생성 메서드 (v2) ====================

    /**
     * v2 API용 생성 메서드
     */
    public static Souvenir ofV2(
        SouvenirRequest request,
        User user,
        PriceInfo originalPrice,
        Integer exchangeAmount,
        String currencySymbol
    ) {
        return Souvenir.builder()
            .name(request.name())
            // v2 필드
            .originalAmount(originalPrice != null ? originalPrice.getAmount() : null)
            .originalCurrency(originalPrice != null ? originalPrice.getCurrency() : null)
            .exchangeAmount(exchangeAmount)
            // 하위 호환 필드 (기존 API용)
            .localPrice(originalPrice != null ? originalPrice.getAmount() : null)
            .currencySymbol(currencySymbol)
            .krwPrice(exchangeAmount)
            // 공통 필드
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

    // ==================== 업데이트 메서드 (v1 - Deprecated) ====================

    /**
     * @deprecated v2 API에서는 updateV2() 사용 권장
     */
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

    // ==================== 업데이트 메서드 (v2) ====================

    /**
     * v2 API용 업데이트 메서드
     */
    public void updateV2(
        SouvenirRequest request,
        PriceInfo originalPrice,
        Integer exchangeAmount,
        String currencySymbol
    ) {
        this.name = request.name();
        // v2 필드
        this.originalAmount = originalPrice != null ? originalPrice.getAmount() : null;
        this.originalCurrency = originalPrice != null ? originalPrice.getCurrency() : null;
        this.exchangeAmount = exchangeAmount;
        // 하위 호환 필드 (기존 API용)
        this.localPrice = originalPrice != null ? originalPrice.getAmount() : null;
        this.currencySymbol = currencySymbol;
        this.krwPrice = exchangeAmount;
        // 공통 필드
        this.description = request.description();
        this.address = request.address();
        this.locationDetail = request.locationDetail();
        this.latitude = request.latitude();
        this.longitude = request.longitude();
        this.category = request.category();
        this.purpose = request.purpose();
        this.countryCode = request.countryCode();
    }

    // ==================== 조회 메서드 ====================

    /**
     * v2 API용: PriceInfo 반환
     */
    public PriceInfo getOriginalPrice() {
        if (originalAmount == null || originalCurrency == null) {
            return null;
        }
        return PriceInfo.of(originalAmount, originalCurrency);
    }

    // ==================== 공통 메서드 ====================

    public void delete() {
        this.deleted = true;
    }

    public boolean isOwnedBy(String userId) {
        return this.user.getUserId().equals(userId);
    }
}
