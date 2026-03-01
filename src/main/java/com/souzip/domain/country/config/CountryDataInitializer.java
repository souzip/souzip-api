package com.souzip.domain.country.config;

import com.souzip.domain.country.service.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("dev")
public class CountryDataInitializer implements ApplicationRunner {

    private final CountryService countryService;

    @Override
    public void run(ApplicationArguments args) {
        countryService.fetchAndSaveCountries();
        log.info("국가 데이터 초기화가 완료되었습니다.");
    }
}
