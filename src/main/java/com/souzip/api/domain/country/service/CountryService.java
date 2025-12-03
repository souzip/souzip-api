package com.souzip.api.domain.country.service;

import com.souzip.api.domain.country.dto.CountryExternalDto;
import com.souzip.api.domain.country.dto.CountryListResponse;
import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.domain.currency.entity.Currency;
import com.souzip.api.domain.currency.repository.CurrencyRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;

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

    public CountryListResponse getCountriesByRegion(String englishName) {
        Region region = getRegionOrThrow(englishName);
        List<Country> countries = countryRepository.findByRegion(region);
        return convertToListResponse(countries);
    }

    public CountryListResponse searchCountriesByName(String name) {
        List<Country> countries = countryRepository.findByNameContaining(name);
        return convertToListResponse(countries);
    }

    public long getCountryCountByRegion(String englishName) {
        Region region = getRegionOrThrow(englishName);
        return countryRepository.countByRegion(region);
    }

    @Transactional
    public void fetchAndSaveCountries() {
        List<CountryExternalDto> externalCountries = externalApiClient.fetchCountries();
        Set<String> existingCodes = getExistingCountryCodes();

        List<Country> newCountries = extractNewCountries(externalCountries, existingCodes);

        if (hasNoNewCountries(newCountries)) {
            log.info("저장할 새로운 국가가 없습니다");
            return;
        }

        countryRepository.saveAll(newCountries);
    }

    private Country findCountryByCodeOrThrow(String code) {
        return countryRepository.findByCode(code)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
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
            .filter(dto -> isNewCountry(dto, existingCodes))
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

        if (isInvalidCurrencyCode(currencyCode)) {
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

    private boolean isNewCountry(CountryExternalDto dto, Set<String> existingCodes) {
        return !existingCodes.contains(dto.cca2());
    }

    private boolean hasNoNewCountries(List<Country> newCountries) {
        return newCountries.isEmpty();
    }

    private boolean isInvalidCurrencyCode(String currencyCode) {
        return currencyCode.isBlank();
    }
}
