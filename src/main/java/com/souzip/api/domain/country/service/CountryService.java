package com.souzip.api.domain.country.service;

import com.souzip.api.domain.country.dto.CountryExternalDto;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate;

    @Value("${external.api.countries-base-url}")
    private String baseUrl;

    private static final String COUNTRIES_API_PATH = "/all?fields=name,capital,region,flags,cca2,latlng";

    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    public Country getCountryByCode(String code) {
        return countryRepository.findByCode(code)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    public List<Country> getCountriesByRegion(String regionCode) {
        return countryRepository.findByRegion(getRegionOrThrow(regionCode));
    }

    public List<Country> searchCountriesByName(String name) {
        return countryRepository.findByNameContaining(name);
    }

    public long getCountryCountByRegion(String regionCode) {
        return countryRepository.countByRegion(getRegionOrThrow(regionCode));
    }

    @Transactional
    public void fetchAndSaveCountries() {
        List<CountryExternalDto> externalCountries = fetchFromExternalApi();
        Set<String> existingCodes = getExistingCountryCodes();

        List<Country> newCountries = externalCountries.stream()
            .filter(dto -> isNewCountry(dto, existingCodes))
            .map(this::convertToEntity)
            .toList();

        if (newCountries.isEmpty()) {
            log.info("저장할 새로운 국가가 없습니다");
            return;
        }

        countryRepository.saveAll(newCountries);
    }

    private Set<String> getExistingCountryCodes() {
        return countryRepository.findAll().stream()
            .map(Country::getCode)
            .collect(Collectors.toSet());
    }

    private boolean isNewCountry(CountryExternalDto dto, Set<String> existingCodes) {
        return !existingCodes.contains(dto.cca2());
    }

    private Country convertToEntity(CountryExternalDto dto) {
        Region region = getRegionOrThrow(dto);
        return dto.toEntity(region);
    }

    private Region getRegionOrThrow(CountryExternalDto dto) {
        return dto.parseRegion()
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_REGION_INVALID));
    }

    private Region getRegionOrThrow(String regionCode) {
        return Region.fromCode(regionCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_REGION_INVALID));
    }

    private List<CountryExternalDto> fetchFromExternalApi() {
        String url = baseUrl + COUNTRIES_API_PATH;
        CountryExternalDto[] response = restTemplate.getForObject(url, CountryExternalDto[].class);

        if (isEmptyResponse(response)) {
            log.warn("외부 API로부터 국가 데이터를 받지 못했습니다.");
            return List.of();
        }
        return Arrays.asList(response);
    }

    private boolean isEmptyResponse(CountryExternalDto[] response) {
        return response == null || response.length == 0;
    }
}
