package com.souzip.domain.currency.controller;

import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.currency.dto.CurrencyResponse;
import com.souzip.domain.currency.service.CurrencyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrencyControllerTest extends RestDocsSupport {

    private final CurrencyService currencyService = mock(CurrencyService.class);

    @Override
    protected Object initController() {
        return new CurrencyController(currencyService);
    }

    @Test
    @DisplayName("국가 코드로 통화 정보를 조회한다")
    void getCurrencyByCountryCode() throws Exception {
        // given
        CurrencyResponse currency = new CurrencyResponse("KRW", "₩");
        given(currencyService.getCurrencyByCountryCode("KR")).willReturn(currency);

        // when & then
        mockMvc.perform(get("/api/currency/{countryCode}", "KR"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.code").value("KRW"))
            .andExpect(jsonPath("$.data.symbol").value("₩"))
            .andDo(document("currency/get-by-country",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("countryCode").description("국가 코드 (예: KR, JP, US)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                    fieldWithPath("data.code").type(JsonFieldType.STRING).description("통화 코드"),
                    fieldWithPath("data.symbol").type(JsonFieldType.STRING).description("통화 기호"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }
}
