package com.souzip.api.domain.country.service;

import com.souzip.api.domain.country.dto.CountryExternalDto;
import com.souzip.api.domain.country.dto.CountryListResponse;
import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import com.souzip.api.domain.country.repository.CountryRepository;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryExternalApiClient externalApiClient;

    public CountryListResponse getAllCountries() {
        List<Country> countries = countryRepository.findAll();
        return convertToListResponse(countries);
    }

    public CountryResponseDto getCountryByCode(String code) {
        Country country = countryRepository.findByCode(code)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
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

        List<Country> newCountries = externalCountries.stream()
            .filter(dto -> !existingCodes.contains(dto.cca2()))
            .map(this::convertToEntity)
            .toList();

        if (newCountries.isEmpty()) {
            log.info("저장할 새로운 국가가 없습니다");
            return;
        }

        countryRepository.saveAll(newCountries);
        log.info("{}개의 새로운 국가를 저장했습니다", newCountries.size());
    }

    private CountryListResponse convertToListResponse(List<Country> countries) {
        List<CountryResponseDto> dtos = countries.stream()
            .map(CountryResponseDto::from)
            .toList();
        return CountryListResponse.from(dtos);
    }

    private Set<String> getExistingCountryCodes() {
        return countryRepository.findAll().stream()
            .map(Country::getCode)
            .collect(Collectors.toSet());
    }

    private Country convertToEntity(CountryExternalDto dto) {
        Region region = getRegionOrThrow(dto.region());
        return dto.toEntity(region);
    }

    private Region getRegionOrThrow(String englishName) {
        return Region.from(englishName)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_REGION_INVALID));
    }
}
