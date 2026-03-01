package com.souzip.domain.user.dto;

import com.souzip.domain.user.entity.UserAgreement;

public record UserAgreementInfo(
    Boolean ageVerified,
    Boolean serviceTerms,
    Boolean privacyRequired,
    Boolean locationService,
    Boolean marketingConsent
) {
    public static UserAgreementInfo from(UserAgreement agreement) {
        return new UserAgreementInfo(
            agreement.getAgeVerified(),
            agreement.getServiceTerms(),
            agreement.getPrivacyRequired(),
            agreement.getLocationService(),
            agreement.getMarketingConsent()
        );
    }
}
