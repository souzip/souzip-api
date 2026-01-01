package com.souzip.api.domain.souvenir.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.souvenir.dto.*;
import com.souzip.api.domain.souvenir.entity.Purpose;
import com.souzip.api.domain.souvenir.service.SouvenirService;
import com.souzip.api.global.security.annotation.CurrentUserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SouvenirControllerTest extends RestDocsSupport {

    private final SouvenirService souvenirService = org.mockito.Mockito.mock(SouvenirService.class);

    @Override
    protected Object initController() {
        return new SouvenirController(souvenirService);
    }

    @BeforeEach
    void setUpWithResolver(RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(CurrentUserId.class)
                                && parameter.getParameterType().equals(Long.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter,
                                                  ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest,
                                                  WebDataBinderFactory binderFactory) {
                        return 1L;
                    }
                })
                .apply(documentationConfiguration(provider))
                .build();
    }

    @Test
    @DisplayName("근처 기념품 조회")
    void getNearbySouvenirs() throws Exception {
        double userLatitude = 40.7128123;
        double userLongitude = -74.0060123;
        int radiusMeter = 4000;

        List<SouvenirNearbyResponse> nearbySouvenirs = List.of(
                SouvenirNearbyResponse.from(
                        1L,
                        "Souvenir A",
                        Category.SOUVENIR_BASIC,
                        Purpose.GIFT,
                        10000,
                        120000,
                        "$",
                        "https://test-dev-images.kr.object.ncloudstorage.com/1234ab123456/1234a123-e1f2-345b-aa12-d123456dd335.png",
                        new BigDecimal("40.7128123"),
                        new BigDecimal("-74.0060123"),
                        "Some address A"
                ),
                SouvenirNearbyResponse.from(
                        2L,
                        "Souvenir B",
                        Category.FOOD_SNACK,
                        Purpose.GIFT,
                        20000,
                        240000,
                        "$",
                        "https://test-dev-images.kr.object.ncloudstorage.com/1234ab123456/1234a123-e1f2-345b-aa12-d123456dd123.png",
                        new BigDecimal("40.7228123"),
                        new BigDecimal("-74.0010123"),
                        "Some address B"
                )
        );

        given(souvenirService.getNearbySouvenirs(userLatitude, userLongitude, radiusMeter))
                .willReturn(SouvenirNearbyListResponse.from(nearbySouvenirs));

        mockMvc.perform(get("/api/souvenirs/nearby")
                        .param("latitude", String.valueOf(userLatitude))
                        .param("longitude", String.valueOf(userLongitude))
                        .param("radiusMeter", String.valueOf(radiusMeter))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.souvenirs[0].id").value(1L))
                .andExpect(jsonPath("$.data.souvenirs[0].name").value("Souvenir A"))
                .andExpect(jsonPath("$.data.souvenirs[0].category").value("SOUVENIR_BASIC"))
                .andExpect(jsonPath("$.data.souvenirs[0].purpose").value("GIFT"))
                .andExpect(jsonPath("$.data.souvenirs[0].localPrice").value(10000))
                .andExpect(jsonPath("$.data.souvenirs[0].krwPrice").value(120000))
                .andExpect(jsonPath("$.data.souvenirs[0].currencySymbol").value("$"))
                .andExpect(jsonPath("$.data.souvenirs[0].thumbnail").value("https://test-dev-images.kr.object.ncloudstorage.com/1234ab123456/1234a123-e1f2-345b-aa12-d123456dd335.png"))
                .andExpect(jsonPath("$.data.souvenirs[0].latitude").value(40.7128123))
                .andExpect(jsonPath("$.data.souvenirs[0].longitude").value(-74.0060123))
                .andExpect(jsonPath("$.data.souvenirs[0].address").value("Some address A"))
                .andDo(document("souvenirs/get-nearby-souvenirs",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.souvenirs[]").type(JsonFieldType.ARRAY).description("근처 기념품 리스트"),
                                fieldWithPath("data.souvenirs[].id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data.souvenirs[].name").type(JsonFieldType.STRING).description("기념품명"),
                                fieldWithPath("data.souvenirs[].category").type(JsonFieldType.STRING).description("카테고리 ENUM name"),
                                fieldWithPath("data.souvenirs[].purpose").type(JsonFieldType.STRING).description("목적 ENUM name"),
                                fieldWithPath("data.souvenirs[].localPrice").type(JsonFieldType.NUMBER).description("현지 가격 (integer)"),
                                fieldWithPath("data.souvenirs[].krwPrice").type(JsonFieldType.NUMBER).description("원화 가격 (integer)"),
                                fieldWithPath("data.souvenirs[].currencySymbol").type(JsonFieldType.STRING).description("통화 기호").optional(),
                                fieldWithPath("data.souvenirs[].thumbnail").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                                fieldWithPath("data.souvenirs[].latitude").type(JsonFieldType.NUMBER).description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.souvenirs[].longitude").type(JsonFieldType.NUMBER).description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.souvenirs[].address").type(JsonFieldType.STRING).description("기념품 주소"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("기념품 조회")
    void getSouvenir() throws Exception {
        Long souvenirId = 1L;

        List<FileResponse> filesResponse = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 0),
                new FileResponse(2L, "https://example.com/file2.jpg", "file2.jpg", 1)
        );

        SouvenirResponse response = new SouvenirResponse(
                souvenirId,
                "테스트 기념품",
                10000,
                "$",
                95000,
                "테스트 설명",
                "617 N MAIN FALLBROOK CA 92028-1934 USA",
                "2층 빨간색 간판이 있는 장소",
                new BigDecimal("35.689487"),
                new BigDecimal("139.691706"),
                Category.SOUVENIR_BASIC,
                Purpose.GIFT,
                "US",
                "닉네임",
                "https://example.com/profile.jpg",
                true,
                filesResponse
        );

        String jwt = "Bearer test.jwt.token";
        given(souvenirService.getSouvenir(souvenirId, jwt))
                .willReturn(response);

        mockMvc.perform(get("/api/souvenirs/{id}", souvenirId)
                .header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(souvenirId))
                .andExpect(jsonPath("$.data.name").value("테스트 기념품"))
                .andDo(document("souvenirs/get-souvenir",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("기념품명"),
                                fieldWithPath("data.localPrice").type(JsonFieldType.NUMBER).description("현지 가격 (integer)"),
                                fieldWithPath("data.currencySymbol").type(JsonFieldType.STRING).optional().description("현지 통화 기호"),
                                fieldWithPath("data.krwPrice").type(JsonFieldType.NUMBER).description("원화 가격 (integer)"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("기념품 설명"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.locationDetail").type(JsonFieldType.STRING).optional().description("위치 상세 설명"),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리 ENUM name"),
                                fieldWithPath("data.purpose").type(JsonFieldType.STRING).description("목적 ENUM name"),
                                fieldWithPath("data.countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data.isOwned").type(JsonFieldType.BOOLEAN).description("조회자가 소유자인지 여부"),
                                fieldWithPath("data.userNickname").type(JsonFieldType.STRING).description("기념품 소유자 닉네임"),
                                fieldWithPath("data.userProfileImageUrl").type(JsonFieldType.STRING).description("기념품 소유자 프로필 이미지 URL"),
                                fieldWithPath("data.files").type(JsonFieldType.ARRAY).description("업로드된 파일 리스트"),
                                fieldWithPath("data.files[].id").type(JsonFieldType.NUMBER).description("파일 ID (integer)"),
                                fieldWithPath("data.files[].url").type(JsonFieldType.STRING).description("파일 URL"),
                                fieldWithPath("data.files[].originalName").type(JsonFieldType.STRING).description("원본 파일명"),
                                fieldWithPath("data.files[].displayOrder").type(JsonFieldType.NUMBER).description("파일 순서 (integer)"),
                                fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("기념품 생성")
    void createSouvenirWithFiles() throws Exception {

        SouvenirCreateRequest request = new SouvenirCreateRequest(
                "테스트 기념품",
                10000,
                "$",
                95000,
                "테스트 설명",
                "617 N MAIN FALLBROOK CA 92028-1934 USA",
                "2층 빨간색 간판이 있는 장소",
                new BigDecimal("35.689487"),
                new BigDecimal("139.691706"),
                Category.SOUVENIR_BASIC,
                Purpose.GIFT,
                "US",
                Collections.emptyList()
        );

        MockMultipartFile souvenirPart = new MockMultipartFile(
                "souvenir",
                "souvenir.json",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file1 = new MockMultipartFile(
                "files", "file1.jpg", "image/jpeg", "dummy content".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "file2.jpg", "image/jpeg", "dummy content".getBytes()
        );

        List<FileResponse> filesResponse = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 0),
                new FileResponse(2L, "https://example.com/file2.jpg", "file2.jpg", 1)
        );

        SouvenirResponse response = new SouvenirResponse(
                1L,
                "테스트 기념품",
                10000,
                "$",
                95000,
                "테스트 설명",
                "617 N MAIN FALLBROOK CA 92028-1934 USA",
                "2층 빨간색 간판이 있는 장소",
                new BigDecimal("35.689487"),
                new BigDecimal("139.691706"),
                Category.SOUVENIR_BASIC,
                Purpose.GIFT,
                "US",
                "닉네임",
                "https://example.com/profile.jpg",
                true,
                filesResponse
        );

        given(souvenirService.createSouvenir(
                any(SouvenirCreateRequest.class),
                eq(1L),
                any(List.class)
        )).willReturn(response);

        mockMvc.perform(multipart("/api/souvenirs")
                        .file(souvenirPart)
                        .file(file1)
                        .file(file2)
                )
                .andDo(document("souvenirs/create-souvenir",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParts(
                                partWithName("souvenir").description("기념품 정보(JSON) (필수)"),
                                partWithName("files").description("업로드 이미지 파일 리스트 (필수)")
                        ),
                        requestPartFields("souvenir",
                                fieldWithPath("name").description("기념품 이름"),
                                fieldWithPath("localPrice").description("현지 가격 (integer)").optional(),
                                fieldWithPath("currencySymbol").description("현지 통화 기호").optional(),
                                fieldWithPath("krwPrice").description("원화 가격 (integer)").optional(),
                                fieldWithPath("description").description("기념품 설명"),
                                fieldWithPath("address").description("주소"),
                                fieldWithPath("locationDetail").description("상세 위치").optional(),
                                fieldWithPath("latitude").description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("longitude").description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("category").description("카테고리 ENUM name"),
                                fieldWithPath("purpose").description("구매 목적 ENUM name"),
                                fieldWithPath("countryCode").description("국가 코드"),
                                fieldWithPath("files").ignored()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("기념품명"),
                                fieldWithPath("data.localPrice").type(JsonFieldType.NUMBER).description("현지 가격 (integer)"),
                                fieldWithPath("data.currencySymbol").type(JsonFieldType.STRING).description("현지 통화 기호").optional(),
                                fieldWithPath("data.krwPrice").type(JsonFieldType.NUMBER).description("원화 가격 (integer)"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("기념품 설명"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.locationDetail").type(JsonFieldType.STRING).description("위치 상세 설명").optional(),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리 ENUM name"),
                                fieldWithPath("data.purpose").type(JsonFieldType.STRING).description("목적 ENUM name"),
                                fieldWithPath("data.countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data.isOwned").type(JsonFieldType.BOOLEAN).description("조회자가 소유자인지 여부"),
                                fieldWithPath("data.userNickname").type(JsonFieldType.STRING).description("기념품 소유자 닉네임"),
                                fieldWithPath("data.userProfileImageUrl").type(JsonFieldType.STRING).description("기념품 소유자 프로필 이미지 URL"),
                                fieldWithPath("data.files").type(JsonFieldType.ARRAY).description("업로드된 파일 리스트"),
                                fieldWithPath("data.files[].id").type(JsonFieldType.NUMBER).description("파일 ID (integer)"),
                                fieldWithPath("data.files[].url").type(JsonFieldType.STRING).description("파일 URL"),
                                fieldWithPath("data.files[].originalName").type(JsonFieldType.STRING).description("원본 파일명"),
                                fieldWithPath("data.files[].displayOrder").type(JsonFieldType.NUMBER).description("파일 순서 (integer)"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("기념품 수정")
    void updateSouvenir() throws Exception {
        Long souvenirId = 1L;

        SouvenirUpdateRequest requestDto = new SouvenirUpdateRequest(
                "수정된 테스트 기념품",
                10000,
                "$",
                95000,
                "수정된 테스트 설명",
                "617 N MAIN FALLBROOK CA 92028-1934 USA",
                "2층 빨간색 간판이 있는 장소",
                new BigDecimal("35.689487"),
                new BigDecimal("139.691706"),
                Category.SOUVENIR_BASIC,
                Purpose.GIFT,
                "US"
        );

        MockMultipartFile souvenirPart = new MockMultipartFile(
                "souvenir",
                "souvenir.json",
                "application/json",
                objectMapper.writeValueAsBytes(requestDto)
        );

        SouvenirResponse response = new SouvenirResponse(
                1L,
                "수정된 테스트 기념품",
                10000,
                "$",
                95000,
                "수정된 테스트 설명",
                "617 N MAIN FALLBROOK CA 92028-1934 USA",
                "2층 빨간색 간판이 있는 장소",
                new BigDecimal("35.689487"),
                new BigDecimal("139.691706"),
                Category.SOUVENIR_BASIC,
                Purpose.GIFT,
                "US",
                "닉네임",
                "https://example.com/profile.jpg",
                true,
                List.of()
        );

        given(souvenirService.updateSouvenir(
                eq(souvenirId),
                any(SouvenirUpdateRequest.class),
                eq(1L)
        )).willReturn(response);

        mockMvc.perform(multipart("/api/souvenirs/{id}", souvenirId)
                        .file(souvenirPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                )
                .andDo(document("souvenirs/update-souvenir",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParts(
                                partWithName("souvenir").description("기념품 정보(JSON) (필수)")
                        ),
                        requestPartFields("souvenir",
                                fieldWithPath("name").description("기념품 이름"),
                                fieldWithPath("localPrice").description("현지 가격 (integer)").optional(),
                                fieldWithPath("currencySymbol").description("현지 통화 기호").optional(),
                                fieldWithPath("krwPrice").description("원화 가격 (integer)").optional(),
                                fieldWithPath("description").description("기념품 설명"),
                                fieldWithPath("address").description("주소"),
                                fieldWithPath("locationDetail").description("상세 위치").optional(),
                                fieldWithPath("latitude").description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("longitude").description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("category").description("카테고리 ENUM name"),
                                fieldWithPath("purpose").description("구매 목적 ENUM name"),
                                fieldWithPath("countryCode").description("국가 코드")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("기념품명"),
                                fieldWithPath("data.localPrice").type(JsonFieldType.NUMBER).description("현지 가격 (integer)"),
                                fieldWithPath("data.currencySymbol").type(JsonFieldType.STRING).description("현지 통화 기호").optional(),
                                fieldWithPath("data.krwPrice").type(JsonFieldType.NUMBER).description("원화 가격 (integer)"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("기념품 설명"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.locationDetail").type(JsonFieldType.STRING).description("위치 상세 설명").optional(),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리 ENUM name"),
                                fieldWithPath("data.purpose").type(JsonFieldType.STRING).description("목적 ENUM name"),
                                fieldWithPath("data.countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data.isOwned").type(JsonFieldType.BOOLEAN).description("조회자가 소유자인지 여부"),
                                fieldWithPath("data.userNickname").type(JsonFieldType.STRING).description("기념품 소유자 닉네임"),
                                fieldWithPath("data.userProfileImageUrl").type(JsonFieldType.STRING).description("기념품 소유자 프로필 이미지 URL"),
                                fieldWithPath("data.files").ignored(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("기념품 삭제")
    void deleteSouvenir() throws Exception {
        Long souvenirId = 1L;

        mockMvc.perform(delete("/api/souvenirs/{id}", souvenirId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("기념품이 성공적으로 삭제되었습니다."))
                .andDo(document("souvenirs/delete-souvenir",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).optional().description("소프트 딜리트 처리, 응답에는 데이터 포함 X"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }
}
