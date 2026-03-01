package com.souzip.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OnboardingRequest(
    @NotNull(message = "만 14세 이상 여부는 필수입니다")
    Boolean ageVerified,

    @NotNull(message = "서비스 이용약관 동의는 필수입니다")
    Boolean serviceTerms,

    @NotNull(message = "개인정보 수집 및 이용 동의는 필수입니다")
    Boolean privacyRequired,

    @NotNull(message = "위치기반 서비스 이용약관 동의는 필수입니다")
    Boolean locationService,

    @NotNull(message = "마케팅 수신 동의 여부는 필수입니다")
    Boolean marketingConsent,

    String nickname,

    String profileImageColor,

    @NotEmpty(message = "최소 1개 이상의 카테고리를 선택해야 합니다.")
    @Size(min = 1, max = 5, message = "카테고리는 최대 5개까지 선택할 수 있습니다.")
    List<String> categories
) {}
