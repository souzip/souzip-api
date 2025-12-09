package com.souzip.api.domain.exchangerate.scheduler;

import com.souzip.api.domain.exchangerate.client.ExchangeRateExternalApiClient;
import com.souzip.api.domain.exchangerate.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
public class ExchangeRateScheduler {

    private final ExchangeRateService exchangeRateService;

    // 매주 월 ~ 금 오전 9시
    @Scheduled(cron = "0 0 9 * * 1-5", zone = "Asia/Seoul")
    public void fetchExchangeRates() {
        log.info("환율 스케줄러 실행 시작");
        exchangeRateService.fetchAndSaveExchangeRates();
        log.info("환율 스케줄러 실행 완료");
    }
}
