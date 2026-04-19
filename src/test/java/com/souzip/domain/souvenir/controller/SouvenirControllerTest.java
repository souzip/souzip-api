package com.souzip.domain.souvenir.controller;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.auth.adapter.security.annotation.CurrentUserId;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.category.entity.Category;
import com.souzip.domain.souvenir.dto.*;
import com.souzip.domain.souvenir.entity.Purpose;
import com.souzip.domain.souvenir.service.SouvenirService;
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

import java.math.BigDecimal;
import java.util.List;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory
                    ) {
                        return 1L;
                    }
                })
                .apply(documentationConfiguration(provider))
                .build();
    }

    @DisplayName("근처 기념품 조회")
    @Test
    void getNearbySouvenirs() throws Exception {
        double userLatitude = 40.7128123;
        double userLongitude = -74.0060123;
        int radiusMeter = 4000;

        List<SouvenirNearbyResponse> nearbySouvenirs = List.of(
                SouvenirNearbyResponse.from(
                        1L, "Souvenir A", Category.SOUVENIR_BASIC, Purpose.GIFT,
                        10000, 120000, "$",
                        "https://test-dev-images.kr.object.ncloudstorage.com/1234ab123456/1234a123-e1f2-345b-aa12-d123456dd335.png",
                        new BigDecimal("40.7128123"), new BigDecimal("-74.0060123"),
                        "Some address A",
                        5L,
                        null
                ),
                SouvenirNearbyResponse.from(
                        2L, "Souvenir B", Category.FOOD_SNACK, Purpose.GIFT,
                        20000, 240000, "$",
                        "https://test-dev-images.kr.object.ncloudstorage.com/1234ab123456/1234a123-e1f2-345b-aa12-d123456dd123.png",
                        new BigDecimal("40.7228123"), new BigDecimal("-74.0010123"),
                        "Some address B",
                        3L,
                        null
                )
        );

        given(souvenirService.getNearbySouvenirs(userLatitude, userLongitude, radiusMeter, null))
                .willReturn(SouvenirNearbyListResponse.from(nearbySouvenirs));

        mockMvc.perform(get("/api/souvenirs/nearby")
                        .param("latitude", String.valueOf(userLatitude))
                        .param("longitude", String.valueOf(userLongitude))
                        .param("radiusMeter", String.valueOf(radiusMeter)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.souvenirs[0].id").value(1L))
                .andExpect(jsonPath("$.data.souvenirs[0].name").value("Souvenir A"))
                .andDo(document("souvenirs/get-nearby-souvenirs",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("latitude").description("위도 (decimal, 소수점 7자리까지)"),
                                parameterWithName("longitude").description("경도 (decimal, 소수점 7자리까지)"),
                                parameterWithName("radiusMeter").description("조회 반경 (미터 단위, 기본값 5000m)").optional()
                        ),
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
                                fieldWithPath("data.souvenirs[].wishlistCount").type(JsonFieldType.NUMBER).description("찜 수"),
                                fieldWithPath("data.souvenirs[].isWishlisted").type(JsonFieldType.BOOLEAN).description("찜 여부").optional(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("기념품 조회")
    @Test
    void getSouvenir() throws Exception {
        // TODO: v1 호환 필드(localPrice, currencySymbol, krwPrice) 향후 제거 예정
        Long souvenirId = 1L;

        List<FileResponse> filesResponse = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 0),
                new FileResponse(2L, "https://example.com/file2.jpg", "file2.jpg", 1)
        );

        PriceResponse priceResponse = new PriceResponse(
                new PriceResponse.PriceDetail(10000, "$"),
                new PriceResponse.PriceDetail(95000, "₩")
        );

        SouvenirDetailResponse response = new SouvenirDetailResponse(
                souvenirId,
                "테스트 기념품",
                10000,  // v1 호환용 - deprecated
                "$",    // v1 호환용 - deprecated
                95000,  // v1 호환용 - deprecated
                priceResponse,
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
                true,
                15,
                filesResponse
        );

        String jwt = "Bearer test.jwt.token";
        given(souvenirService.getSouvenir(souvenirId, jwt)).willReturn(response);

        mockMvc.perform(get("/api/souvenirs/{id}", souvenirId)
                        .header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("souvenirs/get-souvenir",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("id").description("기념품 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("기념품명"),
                                fieldWithPath("data.localPrice").type(JsonFieldType.NUMBER).description("**[Deprecated]** 현지 가격 (integer) - price.original.amount 사용 권장").optional(),
                                fieldWithPath("data.currencySymbol").type(JsonFieldType.STRING).description("**[Deprecated]** 현지 통화 기호 - price.original.symbol 사용 권장").optional(),
                                fieldWithPath("data.krwPrice").type(JsonFieldType.NUMBER).description("**[Deprecated]** 원화 가격 (integer) - price.converted.amount 사용 권장").optional(),
                                fieldWithPath("data.price").type(JsonFieldType.OBJECT).description("v2 가격 정보").optional(),
                                fieldWithPath("data.price.original").type(JsonFieldType.OBJECT).description("사용자가 입력한 원본 가격").optional(),
                                fieldWithPath("data.price.original.amount").type(JsonFieldType.NUMBER).description("원본 금액").optional(),
                                fieldWithPath("data.price.original.symbol").type(JsonFieldType.STRING).description("원본 통화 심볼").optional(),
                                fieldWithPath("data.price.converted").type(JsonFieldType.OBJECT).description("등록 시점 환율로 변환된 가격").optional(),
                                fieldWithPath("data.price.converted.amount").type(JsonFieldType.NUMBER).description("변환 금액").optional(),
                                fieldWithPath("data.price.converted.symbol").type(JsonFieldType.STRING).description("변환 통화 심볼").optional(),
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
                                fieldWithPath("data.isWishlisted").type(JsonFieldType.BOOLEAN).description("찜 여부").optional(),
                                fieldWithPath("data.wishlistCount").type(JsonFieldType.NUMBER).description("찜 수"),
                                fieldWithPath("data.files").type(JsonFieldType.ARRAY).description("업로드된 파일 리스트"),
                                fieldWithPath("data.files[].id").type(JsonFieldType.NUMBER).description("파일 ID (integer)"),
                                fieldWithPath("data.files[].url").type(JsonFieldType.STRING).description("파일 URL"),
                                fieldWithPath("data.files[].originalName").type(JsonFieldType.STRING).description("원본 파일명"),
                                fieldWithPath("data.files[].displayOrder").type(JsonFieldType.NUMBER).description("파일 순서 (integer)"),
                                fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지")
                        )
                ));
    }

    @DisplayName("기념품 생성 (v1)")
    @Test
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
                "US"
        );

        MockMultipartFile souvenirPart = new MockMultipartFile(
                "souvenir",
                "souvenir.json",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file1 = new MockMultipartFile("files", "file1.jpg", "image/jpeg", "dummy content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "file2.jpg", "image/jpeg", "dummy content".getBytes());

        List<FileResponse> filesResponse = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 0),
                new FileResponse(2L, "https://example.com/file2.jpg", "file2.jpg", 1)
        );

        PriceResponse priceResponse = new PriceResponse(
                new PriceResponse.PriceDetail(10000, "$"),
                new PriceResponse.PriceDetail(95000, "₩")
        );

        SouvenirResponse response = new SouvenirResponse(
                1L, "테스트 기념품",
                10000, "$", 95000,
                priceResponse,
                "테스트 설명",
                "617 N MAIN FALLBROOK CA 92028-1934 USA",
                "2층 빨간색 간판이 있는 장소",
                new BigDecimal("35.689487"),
                new BigDecimal("139.691706"),
                Category.SOUVENIR_BASIC, Purpose.GIFT, "US",
                "닉네임", "https://example.com/profile.jpg",
                filesResponse
        );

        given(souvenirService.createSouvenir(any(SouvenirCreateRequest.class), eq(1L), any(List.class)))
                .willReturn(response);

        mockMvc.perform(multipart("/api/souvenirs")
                        .file(souvenirPart)
                        .file(file1)
                        .file(file2))
                .andDo(print())
                .andExpect(status().isOk())
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
                                fieldWithPath("countryCode").description("국가 코드")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("기념품명"),
                                fieldWithPath("data.localPrice").type(JsonFieldType.NUMBER).description("현지 가격 (integer, v1 호환용)").optional(),
                                fieldWithPath("data.currencySymbol").type(JsonFieldType.STRING).description("현지 통화 기호 (v1 호환용)").optional(),
                                fieldWithPath("data.krwPrice").type(JsonFieldType.NUMBER).description("원화 가격 (integer, v1 호환용)").optional(),
                                fieldWithPath("data.price").type(JsonFieldType.OBJECT).description("v2 가격 정보").optional(),
                                fieldWithPath("data.price.original").type(JsonFieldType.OBJECT).description("사용자가 입력한 원본 가격").optional(),
                                fieldWithPath("data.price.original.amount").type(JsonFieldType.NUMBER).description("원본 금액").optional(),
                                fieldWithPath("data.price.original.symbol").type(JsonFieldType.STRING).description("원본 통화 심볼").optional(),
                                fieldWithPath("data.price.converted").type(JsonFieldType.OBJECT).description("등록 시점 환율로 변환된 가격").optional(),
                                fieldWithPath("data.price.converted.amount").type(JsonFieldType.NUMBER).description("변환 금액").optional(),
                                fieldWithPath("data.price.converted.symbol").type(JsonFieldType.STRING).description("변환 통화 심볼").optional(),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("기념품 설명"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.locationDetail").type(JsonFieldType.STRING).description("위치 상세 설명").optional(),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리 ENUM name"),
                                fieldWithPath("data.purpose").type(JsonFieldType.STRING).description("목적 ENUM name"),
                                fieldWithPath("data.countryCode").type(JsonFieldType.STRING).description("국가 코드"),
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

    @DisplayName("기념품 생성 (v2)")
    @Test
    void createSouvenirV2WithFiles() throws Exception {
        // TODO: v1 호환 필드(localPrice, currencySymbol, krwPrice) 향후 제거 예정
        SouvenirRequest request = new SouvenirRequest(
                "테스트 기념품 v2",
                1000,
                "JPY",
                "v2 API 테스트",
                "일본 도쿄 미나토구",
                "도쿄 타워 1층",
                new BigDecimal("35.6586"),
                new BigDecimal("139.7454"),
                Category.SOUVENIR_BASIC,
                Purpose.GIFT,
                "JP"
        );

        MockMultipartFile souvenirPart = new MockMultipartFile(
                "souvenir",
                "souvenir.json",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file1 = new MockMultipartFile("files", "file1.jpg", "image/jpeg", "dummy content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "file2.jpg", "image/jpeg", "dummy content".getBytes());

        List<FileResponse> filesResponse = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 0),
                new FileResponse(2L, "https://example.com/file2.jpg", "file2.jpg", 1)
        );

        PriceResponse priceResponse = new PriceResponse(
                new PriceResponse.PriceDetail(1000, "¥"),
                new PriceResponse.PriceDetail(9337, "₩")
        );

        SouvenirResponse response = new SouvenirResponse(
                1L, "테스트 기념품 v2",
                1000, "¥", 9337,  // v1 호환용 - deprecated
                priceResponse,
                "v2 API 테스트",
                "일본 도쿄 미나토구",
                "도쿄 타워 1층",
                new BigDecimal("35.6586"),
                new BigDecimal("139.7454"),
                Category.SOUVENIR_BASIC, Purpose.GIFT, "JP",
                "닉네임", "https://example.com/profile.jpg",
                filesResponse
        );

        given(souvenirService.createSouvenirV2(any(SouvenirRequest.class), eq(1L), any(List.class)))
                .willReturn(response);

        mockMvc.perform(multipart("/api/v2/souvenirs")
                        .file(souvenirPart)
                        .file(file1)
                        .file(file2))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("souvenirs/create-souvenir-v2",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParts(
                                partWithName("souvenir").description("기념품 정보(JSON) (필수)"),
                                partWithName("files").description("업로드 이미지 파일 리스트 (필수)")
                        ),
                        requestPartFields("souvenir",
                                fieldWithPath("name").description("기념품 이름"),
                                fieldWithPath("price").description("가격 (integer)").optional(),
                                fieldWithPath("currency").description("ISO 4217 통화 코드 (예: JPY, USD, KRW)").optional(),
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
                                fieldWithPath("data.localPrice").type(JsonFieldType.NUMBER).description("**[Deprecated]** 현지 가격 (integer) - price.original.amount 사용 권장").optional(),
                                fieldWithPath("data.currencySymbol").type(JsonFieldType.STRING).description("**[Deprecated]** 현지 통화 기호 - price.original.symbol 사용 권장").optional(),
                                fieldWithPath("data.krwPrice").type(JsonFieldType.NUMBER).description("**[Deprecated]** 원화 가격 (integer) - price.converted.amount 사용 권장").optional(),
                                fieldWithPath("data.price").type(JsonFieldType.OBJECT).description("v2 가격 정보"),
                                fieldWithPath("data.price.original").type(JsonFieldType.OBJECT).description("사용자가 입력한 원본 가격"),
                                fieldWithPath("data.price.original.amount").type(JsonFieldType.NUMBER).description("원본 금액"),
                                fieldWithPath("data.price.original.symbol").type(JsonFieldType.STRING).description("원본 통화 심볼"),
                                fieldWithPath("data.price.converted").type(JsonFieldType.OBJECT).description("등록 시점 환율로 변환된 가격"),
                                fieldWithPath("data.price.converted.amount").type(JsonFieldType.NUMBER).description("변환 금액"),
                                fieldWithPath("data.price.converted.symbol").type(JsonFieldType.STRING).description("변환 통화 심볼"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("기념품 설명"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.locationDetail").type(JsonFieldType.STRING).description("위치 상세 설명").optional(),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리 ENUM name"),
                                fieldWithPath("data.purpose").type(JsonFieldType.STRING).description("목적 ENUM name"),
                                fieldWithPath("data.countryCode").type(JsonFieldType.STRING).description("국가 코드"),
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

    @DisplayName("기념품 수정 (v1)")
    @Test
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

        PriceResponse priceResponse = new PriceResponse(
                new PriceResponse.PriceDetail(10000, "$"),
                new PriceResponse.PriceDetail(95000, "₩")
        );

        SouvenirResponse response = new SouvenirResponse(
                1L, "수정된 테스트 기념품",
                10000, "$", 95000,
                priceResponse,
                "수정된 테스트 설명",
                "617 N MAIN FALLBROOK CA 92028-1934 USA",
                "2층 빨간색 간판이 있는 장소",
                new BigDecimal("35.689487"),
                new BigDecimal("139.691706"),
                Category.SOUVENIR_BASIC, Purpose.GIFT, "US",
                "닉네임", "https://example.com/profile.jpg",
                List.of()
        );

        given(souvenirService.updateSouvenir(eq(souvenirId), any(SouvenirUpdateRequest.class), eq(1L)))
                .willReturn(response);

        mockMvc.perform(multipart("/api/souvenirs/{id}", souvenirId)
                        .file(souvenirPart)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("souvenirs/update-souvenir",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("id").description("기념품 ID")
                        ),
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
                                fieldWithPath("data.localPrice").type(JsonFieldType.NUMBER).description("현지 가격 (integer, v1 호환용)").optional(),
                                fieldWithPath("data.currencySymbol").type(JsonFieldType.STRING).description("현지 통화 기호 (v1 호환용)").optional(),
                                fieldWithPath("data.krwPrice").type(JsonFieldType.NUMBER).description("원화 가격 (integer, v1 호환용)").optional(),
                                fieldWithPath("data.price").type(JsonFieldType.OBJECT).description("v2 가격 정보").optional(),
                                fieldWithPath("data.price.original").type(JsonFieldType.OBJECT).description("사용자가 입력한 원본 가격").optional(),
                                fieldWithPath("data.price.original.amount").type(JsonFieldType.NUMBER).description("원본 금액").optional(),
                                fieldWithPath("data.price.original.symbol").type(JsonFieldType.STRING).description("원본 통화 심볼").optional(),
                                fieldWithPath("data.price.converted").type(JsonFieldType.OBJECT).description("등록 시점 환율로 변환된 가격").optional(),
                                fieldWithPath("data.price.converted.amount").type(JsonFieldType.NUMBER).description("변환 금액").optional(),
                                fieldWithPath("data.price.converted.symbol").type(JsonFieldType.STRING).description("변환 통화 심볼").optional(),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("기념품 설명"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.locationDetail").type(JsonFieldType.STRING).description("위치 상세 설명").optional(),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리 ENUM name"),
                                fieldWithPath("data.purpose").type(JsonFieldType.STRING).description("목적 ENUM name"),
                                fieldWithPath("data.countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data.userNickname").type(JsonFieldType.STRING).description("기념품 소유자 닉네임"),
                                fieldWithPath("data.userProfileImageUrl").type(JsonFieldType.STRING).description("기념품 소유자 프로필 이미지 URL"),
                                fieldWithPath("data.files").ignored(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("기념품 수정 (v2)")
    @Test
    void updateSouvenirV2() throws Exception {
        // TODO: v1 호환 필드(localPrice, currencySymbol, krwPrice) 향후 제거 예정
        Long souvenirId = 1L;

        SouvenirRequest requestDto = new SouvenirRequest(
                "수정된 기념품 v2",
                1500,
                "JPY",
                "v2 수정 테스트",
                "일본 도쿄 미나토구",
                "도쿄 타워 2층",
                new BigDecimal("35.6586"),
                new BigDecimal("139.7454"),
                Category.SOUVENIR_BASIC,
                Purpose.GIFT,
                "JP"
        );

        MockMultipartFile souvenirPart = new MockMultipartFile(
                "souvenir",
                "souvenir.json",
                "application/json",
                objectMapper.writeValueAsBytes(requestDto)
        );

        PriceResponse priceResponse = new PriceResponse(
                new PriceResponse.PriceDetail(1500, "¥"),
                new PriceResponse.PriceDetail(14005, "₩")
        );

        SouvenirResponse response = new SouvenirResponse(
                1L, "수정된 기념품 v2",
                1500, "¥", 14005,  // v1 호환용 - deprecated
                priceResponse,
                "v2 수정 테스트",
                "일본 도쿄 미나토구",
                "도쿄 타워 2층",
                new BigDecimal("35.6586"),
                new BigDecimal("139.7454"),
                Category.SOUVENIR_BASIC, Purpose.GIFT, "JP",
                "닉네임", "https://example.com/profile.jpg",
                List.of()
        );

        given(souvenirService.updateSouvenirV2(eq(souvenirId), any(SouvenirRequest.class), eq(1L)))
                .willReturn(response);

        mockMvc.perform(multipart("/api/v2/souvenirs/{id}", souvenirId)
                        .file(souvenirPart)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("souvenirs/update-souvenir-v2",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("id").description("기념품 ID")
                        ),
                        requestParts(
                                partWithName("souvenir").description("기념품 정보(JSON) (필수)")
                        ),
                        requestPartFields("souvenir",
                                fieldWithPath("name").description("기념품 이름"),
                                fieldWithPath("price").description("가격 (integer)").optional(),
                                fieldWithPath("currency").description("ISO 4217 통화 코드 (예: JPY, USD, KRW)").optional(),
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
                                fieldWithPath("data.localPrice").type(JsonFieldType.NUMBER).description("**[Deprecated]** 현지 가격 (integer) - price.original.amount 사용 권장").optional(),
                                fieldWithPath("data.currencySymbol").type(JsonFieldType.STRING).description("**[Deprecated]** 현지 통화 기호 - price.original.symbol 사용 권장").optional(),
                                fieldWithPath("data.krwPrice").type(JsonFieldType.NUMBER).description("**[Deprecated]** 원화 가격 (integer) - price.converted.amount 사용 권장").optional(),
                                fieldWithPath("data.price").type(JsonFieldType.OBJECT).description("v2 가격 정보"),
                                fieldWithPath("data.price.original").type(JsonFieldType.OBJECT).description("사용자가 입력한 원본 가격"),
                                fieldWithPath("data.price.original.amount").type(JsonFieldType.NUMBER).description("원본 금액"),
                                fieldWithPath("data.price.original.symbol").type(JsonFieldType.STRING).description("원본 통화 심볼"),
                                fieldWithPath("data.price.converted").type(JsonFieldType.OBJECT).description("등록 시점 환율로 변환된 가격"),
                                fieldWithPath("data.price.converted.amount").type(JsonFieldType.NUMBER).description("변환 금액"),
                                fieldWithPath("data.price.converted.symbol").type(JsonFieldType.STRING).description("변환 통화 심볼"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("기념품 설명"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.locationDetail").type(JsonFieldType.STRING).description("위치 상세 설명").optional(),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도 (decimal, 소수점 7자리까지)"),
                                fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리 ENUM name"),
                                fieldWithPath("data.purpose").type(JsonFieldType.STRING).description("목적 ENUM name"),
                                fieldWithPath("data.countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data.userNickname").type(JsonFieldType.STRING).description("기념품 소유자 닉네임"),
                                fieldWithPath("data.userProfileImageUrl").type(JsonFieldType.STRING).description("기념품 소유자 프로필 이미지 URL"),
                                fieldWithPath("data.files").ignored(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("기념품 삭제")
    @Test
    void deleteSouvenir() throws Exception {
        Long souvenirId = 1L;

        mockMvc.perform(delete("/api/souvenirs/{id}", souvenirId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andDo(document("souvenirs/delete-souvenir",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("id").description("기념품 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("소프트 딜리트 처리, 응답에는 데이터 포함 X"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지").optional()
                        )
                ));
    }
}
