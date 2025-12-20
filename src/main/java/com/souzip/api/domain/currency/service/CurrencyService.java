package com.souzip.api.domain.currency.service;

import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.domain.currency.dto.CurrencyResponse;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CurrencyService {

    private final CountryRepository countryRepository;

    public CurrencyResponse getCurrencyByCountryCode(String countryCode) {
        Country country = countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));

        return CurrencyResponse.from(country.getCurrency());
    }
}
