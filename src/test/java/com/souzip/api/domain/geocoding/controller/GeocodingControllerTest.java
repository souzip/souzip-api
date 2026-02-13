package com.souzip.api.domain.geocoding.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.geocoding.dto.GeocodingAddressResponse;
import com.souzip.api.domain.geocoding.service.GeocodingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GeocodingControllerTest extends RestDocsSupport {

    private final GeocodingService geocodingService = mock(GeocodingService.class);

    @Override
    protected Object initController() {
        return new GeocodingController(geocodingService);
    }

    @Test
    @DisplayName("위도와 경도를 통해 주소를 조회할 수 있다.")
    void getAddress() throws Exception {
        double latitude = 37.5665121;
        double longitude = 126.9780123;

        GeocodingAddressResponse response = new GeocodingAddressResponse(
            "110 Sejong-daero, Jung District, Seoul, South Korea",
            "Seoul",
            "KR"
        );

        given(geocodingService.getAddress(latitude, longitude)).willReturn(response);

        mockMvc.perform(get("/api/geocoding/address")
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.formattedAddress").value("110 Sejong-daero, Jung District, Seoul, South Korea"))
            .andExpect(jsonPath("$.data.city").value("Seoul"))
            .andExpect(jsonPath("$.data.countryCode").value("KR"))
            .andDo(document("geocoding/get-address",
                getDocumentRequest(),
                getDocumentResponse(),
                queryParameters(
                    parameterWithName("latitude").description("위도 (decimal, 소수점 7자리까지)"),
                    parameterWithName("longitude").description("경도 (decimal, 소수점 7자리까지)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                    fieldWithPath("data.formattedAddress").type(JsonFieldType.STRING).description("전체 주소"),
                    fieldWithPath("data.city").type(JsonFieldType.STRING).description("도시 이름"),
                    fieldWithPath("data.countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }
}
