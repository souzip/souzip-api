package com.souzip.api.domain.country.service;

import com.souzip.api.domain.country.dto.CountryExternalDto;
import com.souzip.api.domain.country.entity.Country;
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
@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate;

    @Value("${external.api.countries-base-url}")
    private String baseUrl;

    @Transactional
    public void fetchAndSaveCountries() {
        List<CountryExternalDto> externalCountries = fetchFromExternalApi();

        Set<Object> existingCodes = getExistingCountryCodes();

        List<Country> newCountries = externalCountries.stream()
            .filter(dto -> isNewCountry(dto.cca2(), existingCodes))
            .map(CountryExternalDto::toEntity)
            .toList();

        if (newCountries.isEmpty()) {
            log.info("저장할 새로운 국가가 없습니다");
            return;
        }

        countryRepository.saveAll(newCountries);

    }

    @Transactional(readOnly = true)
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Country getCountryByCode(String code) {
        return countryRepository.findByCode(code)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Country> getCountriesByRegion(String region) {
        return countryRepository.findByRegion(region);
    }

    @Transactional(readOnly = true)
    public List<Country> searchCountriesByName(String name) {
        return countryRepository.findByNameContaining(name);
    }

    @Transactional(readOnly = true)
    public long getCountryCountByRegion(String region) {
        return countryRepository.countByRegion(region);
    }

    private List<CountryExternalDto> fetchFromExternalApi() {
        String url = baseUrl + "/all?fields=name,capital,region,flags,cca2,latlng";
        CountryExternalDto[] response = restTemplate.getForObject(url, CountryExternalDto[].class);

        if (isInvalidResponse(response)) {
            log.warn("외부 API로부터 국가 데이터를 받지 못했습니다");
            return List.of();
        }

        return Arrays.asList(response);
    }

    private boolean isInvalidResponse(CountryExternalDto[] response) {
        return response == null || response.length == 0;
    }

    private Set<Object> getExistingCountryCodes() {
        return countryRepository.findAll()
            .stream()
            .map(Country::getCode)
            .collect(Collectors.toSet());
    }

    private boolean isNewCountry(String countryCode, Set<Object> existingCodes) {
        return !existingCodes.contains(countryCode);
    }
}
