package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.souvenir.entity.Purpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * v2 API용 기념품 생성/수정 요청 DTO
 * - price, currency로 통합 (localPrice, currencySymbol, krwPrice 대체)
 * - 서버에서 자동으로 환율 계산
 */
public record SouvenirRequest(

    @NotBlank(message = "기념품 이름은 필수입니다.")
    @Size(max = 30, message = "기념품 이름은 30자 이하여야 합니다.")
    String name,

    @Positive(message = "가격은 0보다 큰 값이어야 합니다.")
    Integer price,

    @Size(min = 3, max = 3, message = "통화 코드는 3자리여야 합니다.")
    String currency,

    @NotBlank(message = "기념품 설명은 필수입니다.")
    @Size(max = 1000, message = "기념품 설명은 1000자 이하여야 합니다.")
    String description,

    @NotBlank(message = "주소는 필수입니다.")
    String address,

    String locationDetail,

    @NotNull(message = "위도는 필수입니다.")
    BigDecimal latitude,

    @NotNull(message = "경도는 필수입니다.")
    BigDecimal longitude,

    @NotNull(message = "카테고리는 필수입니다.")
    Category category,

    @NotNull(message = "기념품 구매 목적은 필수입니다.")
    Purpose purpose,

    @NotBlank(message = "국가 코드는 필수입니다.")
    String countryCode
) {}
