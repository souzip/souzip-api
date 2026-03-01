package com.souzip.domain.exchangerate.service;

import com.souzip.domain.country.dto.CountryResponseDto;
import com.souzip.domain.country.repository.CountryRepository;
import com.souzip.domain.country.service.CountryService;
import com.souzip.domain.currency.entity.Currency;
import com.souzip.domain.currency.repository.CurrencyRepository;
import com.souzip.domain.exchangerate.client.ExchangeRateExternalApiClient;
import com.souzip.domain.exchangerate.dto.ExchangeCalculatedPrice;
import com.souzip.domain.exchangerate.dto.ExchangeRateExternalDto;
import com.souzip.domain.exchangerate.dto.ExchangeRateResponseDto;
import com.souzip.domain.exchangerate.entity.ExchangeRate;
import com.souzip.domain.exchangerate.repository.ExchangeRateRepository;
import com.souzip.domain.souvenir.dto.PriceData;
import com.souzip.domain.souvenir.dto.PriceResponse;
import com.souzip.domain.souvenir.vo.PriceInfo;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ExchangeRateService {

    private static final String DEFAULT_BASE_CURRENCY = "KRW";
    private static final int ZERO_PRICE = 0;

    private final ExchangeRateRepository exchangeRateRepository;
    private final CountryService countryService;
    private final ExchangeRateExternalApiClient apiClient;
    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;

    public ExchangeCalculatedPrice calculatePrice(
        String countryCode,
        Integer localPrice,
        Integer krwPrice
    ) {
        CountryResponseDto country = countryService.getCountryByCode(countryCode);
        ExchangeRateResponseDto rate = getRate(country.currency().code());

        if (hasLocalPrice(localPrice)) {
            return fromLocalPrice(localPrice, rate, country);
        }

        if (hasKrwPrice(krwPrice)) {
            return fromKrwPrice(krwPrice, rate, country);
        }

        return createEmptyPrice(country);
    }

    public PriceData calculatePriceData(
        Integer price,
        String currency,
        String countryCode
    ) {
        if (hasInvalidPriceInput(price, currency)) {
            return createEmptyPriceData();
        }

        PriceInfo originalPrice = PriceInfo.of(price, currency);
        Integer exchangeAmount = convertToKrw(price, currency);
        String currencySymbol = getCurrencySymbol(currency);

        String localCurrency = getLocalCurrency(countryCode);
        PriceInfo convertedPrice = calculateConvertedPrice(
            originalPrice,
            localCurrency,
            exchangeAmount
        );

        return new PriceData(
            originalPrice,
            exchangeAmount,
            currencySymbol,
            convertedPrice
        );
    }

    public PriceResponse createPriceResponse(PriceInfo originalPrice, PriceInfo convertedPrice) {
        if (hasNoOriginalPrice(originalPrice)) {
            return null;
        }

        String originalSymbol = getCurrencySymbol(originalPrice.getCurrency());
        String convertedSymbol = getCurrencySymbol(convertedPrice.getCurrency());

        return PriceResponse.of(
            originalPrice.getAmount(),
            originalSymbol,
            convertedPrice.getAmount(),
            convertedSymbol
        );
    }

    private boolean hasLocalPrice(Integer localPrice) {
        return localPrice != null;
    }

    private boolean hasKrwPrice(Integer krwPrice) {
        return krwPrice != null;
    }

    private ExchangeCalculatedPrice createEmptyPrice(CountryResponseDto country) {
        return new ExchangeCalculatedPrice(
            ZERO_PRICE,
            ZERO_PRICE,
            country.currency().symbol()
        );
    }

    private ExchangeCalculatedPrice fromLocalPrice(
        Integer localPrice,
        ExchangeRateResponseDto rate,
        CountryResponseDto country
    ) {
        int krw = multiply(localPrice, rate.rate());

        return new ExchangeCalculatedPrice(
            localPrice,
            krw,
            country.currency().symbol()
        );
    }

    private ExchangeCalculatedPrice fromKrwPrice(
        Integer krwPrice,
        ExchangeRateResponseDto rate,
        CountryResponseDto country
    ) {
        int local = divide(krwPrice, rate.rate());

        return new ExchangeCalculatedPrice(
            local,
            krwPrice,
            country.currency().symbol()
        );
    }

    public Integer convertToKrw(Integer amount, String fromCurrency) {
        if (isInvalidConversionInput(amount, fromCurrency)) {
            return null;
        }

        if (isAlreadyKrw(fromCurrency)) {
            return amount;
        }

        return calculateKrwAmount(amount, fromCurrency);
    }

    public Integer convertFromKrw(Integer krwAmount, String toCurrency) {
        if (isInvalidConversionInput(krwAmount, toCurrency)) {
            return null;
        }

        if (isAlreadyKrw(toCurrency)) {
            return krwAmount;
        }

        return calculateForeignAmount(krwAmount, toCurrency);
    }

    private boolean hasInvalidPriceInput(Integer price, String currency) {
        return price == null || currency == null;
    }

    private PriceData createEmptyPriceData() {
        return new PriceData(null, null, null, null);
    }

    private PriceInfo calculateConvertedPrice(
        PriceInfo originalPrice,
        String localCurrency,
        Integer exchangeAmount
    ) {
        if (isKrwInput(originalPrice)) {
            return convertKrwToLocal(originalPrice.getAmount(), localCurrency);
        }

        return PriceInfo.of(exchangeAmount, DEFAULT_BASE_CURRENCY);
    }

    private boolean isKrwInput(PriceInfo priceInfo) {
        return DEFAULT_BASE_CURRENCY.equals(priceInfo.getCurrency());
    }

    private PriceInfo convertKrwToLocal(Integer krwAmount, String localCurrency) {
        Integer localAmount = convertFromKrw(krwAmount, localCurrency);
        return PriceInfo.of(localAmount, localCurrency);
    }

    private boolean hasNoOriginalPrice(PriceInfo originalPrice) {
        return originalPrice == null;
    }

    private String getLocalCurrency(String countryCode) {
        return countryRepository.findByCodeWithCurrency(countryCode)
            .map(country -> country.getCurrency().getCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    private String getCurrencySymbol(String currencyCode) {
        if (isNullCurrency(currencyCode)) {
            return null;
        }

        return currencyRepository.findByCode(currencyCode)
            .map(Currency::getSymbol)
            .orElseThrow(() -> new BusinessException(ErrorCode.CURRENCY_NOT_FOUND));
    }

    private boolean isNullCurrency(String currencyCode) {
        return currencyCode == null;
    }

    private boolean isInvalidConversionInput(Integer amount, String currency) {
        return amount == null || currency == null;
    }

    private boolean isAlreadyKrw(String currency) {
        return DEFAULT_BASE_CURRENCY.equals(currency);
    }

    private Integer calculateKrwAmount(Integer amount, String fromCurrency) {
        ExchangeRateResponseDto rate = getRate(fromCurrency);
        return multiply(amount, rate.rate());
    }

    private Integer calculateForeignAmount(Integer krwAmount, String toCurrency) {
        ExchangeRateResponseDto rate = getRate(toCurrency);
        return divide(krwAmount, rate.rate());
    }

    private int multiply(Integer value, BigDecimal rate) {
        return BigDecimal.valueOf(value)
            .multiply(rate)
            .setScale(0, RoundingMode.HALF_UP)
            .intValue();
    }

    private int divide(Integer value, BigDecimal rate) {
        return BigDecimal.valueOf(value)
            .divide(rate, 0, RoundingMode.HALF_UP)
            .intValue();
    }

    public ExchangeRateResponseDto getRate(String currencyCode) {
        ExchangeRate rate = exchangeRateRepository
            .findByCurrencyCodeAndBaseCode(currencyCode, DEFAULT_BASE_CURRENCY)
            .orElseThrow(() -> new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND));

        return ExchangeRateResponseDto.from(rate);
    }

    @Transactional
    public void fetchAndSaveExchangeRates() {
        ExchangeRateExternalDto externalDto = apiClient.fetchRates();
        List<ExchangeRate> rates = externalDto.toEntities();

        if (hasNoRatesToSave(rates)) {
            logNoDataToSave();
            return;
        }

        saveAllRates(rates);
        logSaveComplete(rates.size());
    }

    private boolean hasNoRatesToSave(List<ExchangeRate> rates) {
        return rates.isEmpty();
    }

    private void logNoDataToSave() {
        log.info("저장할 환율 데이터가 없습니다.");
    }

    private void saveAllRates(List<ExchangeRate> rates) {
        rates.forEach(this::saveOrUpdate);
    }

    private void logSaveComplete(int count) {
        log.info("환율 데이터 갱신 완료: {}건", count);
    }

    private void saveOrUpdate(ExchangeRate newRate) {
        Optional<ExchangeRate> existingRate = findExistingRate(newRate);

        if (existingRate.isPresent()) {
            updateExistingRate(existingRate.get(), newRate);
            return;
        }

        saveNewRate(newRate);
    }

    private Optional<ExchangeRate> findExistingRate(ExchangeRate newRate) {
        return exchangeRateRepository.findByCurrencyCodeAndBaseCode(
            newRate.getCurrencyCode(),
            newRate.getBaseCode()
        );
    }

    private void updateExistingRate(ExchangeRate existing, ExchangeRate newRate) {
        existing.updateRate(newRate.getRate());
    }

    private void saveNewRate(ExchangeRate newRate) {
        exchangeRateRepository.save(newRate);
    }
}
