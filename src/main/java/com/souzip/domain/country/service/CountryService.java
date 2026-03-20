package com.souzip.domain.country.service;

import com.souzip.domain.country.client.CountryExternalApiClient;
import com.souzip.domain.country.dto.CountryExternalDto;
import com.souzip.domain.country.dto.CountryListResponse;
import com.souzip.domain.country.dto.CountryResponseDto;
import com.souzip.domain.country.entity.Country;
import com.souzip.domain.country.entity.Region;
import com.souzip.domain.country.repository.CountryRepository;
import com.souzip.domain.currency.entity.Currency;
import com.souzip.domain.currency.repository.CurrencyRepository;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
    private final CountryExternalApiClient externalApiClient;

    public CountryListResponse getAllCountries() {
        List<Country> countries = countryRepository.findAll();
        return convertToListResponse(countries);
    }

    public CountryResponseDto getCountryByCode(String code) {
        Country country = findCountryByCodeOrThrow(code);

        return CountryResponseDto.from(country);
    }

    private Country findCountryByCodeOrThrow(String code) {
        return countryRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    @Transactional
    public void fetchAndSaveCountries() {
        List<CountryExternalDto> externalCountries = externalApiClient.fetchCountries();
        Set<String> existingCodes = getExistingCountryCodes();

        List<Country> newCountries = extractNewCountries(externalCountries, existingCodes);

        if (newCountries.isEmpty()) {
            log.info("저장할 새로운 국가가 없습니다");
            return;
        }

        countryRepository.saveAll(newCountries);
    }

    private Region getRegionOrThrow(String englishName) {
        return Region.from(englishName)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_REGION_INVALID));
    }

    private Set<String> getExistingCountryCodes() {
        return countryRepository.findAll().stream()
                .map(Country::getCode)
                .collect(Collectors.toSet());
    }

    private List<Country> extractNewCountries(
            List<CountryExternalDto> externalCountries,
            Set<String> existingCodes
    ) {
        return externalCountries.stream()
                .filter(dto -> !existingCodes.contains(dto.cca2()))
                .filter(this::hasValidCurrency)
                .map(this::convertToEntity)
                .toList();
    }

    private Country convertToEntity(CountryExternalDto dto) {
        Region region = getRegionOrThrow(dto.region());
        Currency currency = getOrCreateCurrency(dto);
        return dto.toEntity(region, currency);
    }

    private CountryListResponse convertToListResponse(List<Country> countries) {
        return CountryListResponse.from(
                countries.stream()
                        .map(CountryResponseDto::from)
                        .toList()
        );
    }

    private Currency getOrCreateCurrency(CountryExternalDto dto) {
        String currencyCode = dto.getPrimaryCurrencyCode();

        if (currencyCode == null || currencyCode.isBlank()) {
            log.debug("통화 정보 없음 - 국가: {}", dto.cca2());
            return null;
        }

        return currencyRepository.findByCode(currencyCode)
                .orElseGet(() -> createAndSaveCurrency(currencyCode, dto.getPrimaryCurrencySymbol()));
    }

    private Currency createAndSaveCurrency(String currencyCode, String currencySymbol) {
        Currency newCurrency = Currency.of(currencyCode, currencySymbol);
        return currencyRepository.save(newCurrency);
    }

    private boolean hasValidCurrency(CountryExternalDto dto) {
        String currencyCode = dto.getPrimaryCurrencyCode();
        return currencyCode != null && !currencyCode.isBlank();
    }
}
