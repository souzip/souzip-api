package com.souzip.api.domain.user.entity;

import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class UserAgreement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(nullable = false)
    private Boolean ageVerified;

    @Column(nullable = false)
    private Boolean serviceTerms;

    @Column(nullable = false)
    private Boolean privacyRequired;

    @Column(nullable = false)
    private Boolean locationService;

    @Column(nullable = false)
    private Boolean marketingConsent;

    public static UserAgreement of(
        User user,
        Boolean ageVerified,
        Boolean serviceTerms,
        Boolean privacyRequired,
        Boolean locationService,
        Boolean marketingConsent
    ) {
        return UserAgreement.builder()
            .user(user)
            .ageVerified(ageVerified)
            .serviceTerms(serviceTerms)
            .privacyRequired(privacyRequired)
            .locationService(locationService)
            .marketingConsent(marketingConsent)
            .build();
    }

    public void updateAgreements(
        Boolean ageVerified,
        Boolean serviceTerms,
        Boolean privacyRequired,
        Boolean locationService,
        Boolean marketingConsent
    ) {
        this.ageVerified = ageVerified;
        this.serviceTerms = serviceTerms;
        this.privacyRequired = privacyRequired;
        this.locationService = locationService;
        this.marketingConsent = marketingConsent;
    }

    public boolean isAllRequiredAgreed() {
        return ageVerified && serviceTerms && privacyRequired && locationService;
    }
}
