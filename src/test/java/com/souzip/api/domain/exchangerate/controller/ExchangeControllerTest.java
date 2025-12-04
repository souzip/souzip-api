package com.souzip.api.domain.exchangerate.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.exchangerate.dto.ExchangeRateResponseDto;
import com.souzip.api.domain.exchangerate.service.ExchangeRateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

class ExchangeControllerTest extends RestDocsSupport {

    private final ExchangeRateService exchangeRateService = mock(ExchangeRateService.class);

    @Override
    protected Object initController() {
        return new ExchangeController(exchangeRateService);
    }

    private ExchangeRateResponseDto createResponse(String currency, BigDecimal rate) {
        return new ExchangeRateResponseDto("KRW", currency, rate);
    }

    @Test
    @DisplayName("단일 국가 환율을 조회한다.")
    void getRateByCountry() throws Exception {
        // given
        ExchangeRateResponseDto usd = createResponse("USD", BigDecimal.valueOf(1300));

        given(exchangeRateService.getRateByCountry("US"))
            .willReturn(usd);

        // when & then
        mockMvc.perform(get("/api/exchange-rate/{countryCode}", "US"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.baseCode").value("KRW"))
            .andExpect(jsonPath("$.data.currencyCode").value("USD"))
            .andExpect(jsonPath("$.data.rate").value(1300))
            .andDo(document("exchange-rate/get-by-country",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("countryCode").description("나라 코드")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                    fieldWithPath("data.baseCode").type(JsonFieldType.STRING).description("기준 통화 코드"),
                    fieldWithPath("data.currencyCode").type(JsonFieldType.STRING).description("대상 통화 코드"),
                    fieldWithPath("data.rate").type(JsonFieldType.NUMBER).description("환율"),
                    fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지")
                )
            ));
    }

    @Test
    @DisplayName("여러 국가 환율을 조회한다.")
    void getRatesByCountries() throws Exception {
        // given
        ExchangeRateResponseDto usd = createResponse("USD", BigDecimal.valueOf(1300));
        ExchangeRateResponseDto jpy = createResponse("JPY", BigDecimal.valueOf(9));

        Set<String> codes = Set.of("US", "JP");

        given(exchangeRateService.getRatesByCountries(codes))
            .willReturn(List.of(usd, jpy));

        // when & then
        mockMvc.perform(get("/api/exchange-rate")
                .param("countries", "US", "JP"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].currencyCode").value("USD"))
            .andExpect(jsonPath("$.data[1].currencyCode").value("JPY"))
            .andDo(document("exchange-rate/get-by-countries",
                getDocumentRequest(),
                getDocumentResponse(),
                queryParameters(
                    parameterWithName("countries").description("조회할 국가 코드 목록 (여러 개 전달 가능)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.ARRAY).description("응답 데이터"),
                    fieldWithPath("data[].baseCode").type(JsonFieldType.STRING).description("기준 통화 코드"),
                    fieldWithPath("data[].currencyCode").type(JsonFieldType.STRING).description("대상 통화 코드"),
                    fieldWithPath("data[].rate").type(JsonFieldType.NUMBER).description("환율"),
                    fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지")
                )
            ));
    }

    @Test
    @DisplayName("국가 코드를 전달하지 않으면 전체 환율을 조회한다.")
    void getAllRates() throws Exception {
        // given
        ExchangeRateResponseDto usd = createResponse("USD", BigDecimal.valueOf(1300));
        ExchangeRateResponseDto jpy = createResponse("JPY", BigDecimal.valueOf(9));
        ExchangeRateResponseDto eur = createResponse("EUR", BigDecimal.valueOf(1500));

        given(exchangeRateService.getRatesByCountries(null))
            .willReturn(List.of(usd, jpy, eur));

        // when & then
        mockMvc.perform(get("/api/exchange-rate"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[2].currencyCode").value("EUR"))
            .andDo(document("exchange-rate/get-all",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.ARRAY).description("응답 데이터"),
                    fieldWithPath("data[].baseCode").type(JsonFieldType.STRING).description("기준 통화 코드"),
                    fieldWithPath("data[].currencyCode").type(JsonFieldType.STRING).description("대상 통화 코드"),
                    fieldWithPath("data[].rate").type(JsonFieldType.NUMBER).description("환율"),
                    fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지")
                )
            ));
    }
}
