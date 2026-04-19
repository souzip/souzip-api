package com.souzip.domain.wishlist.controller;

import com.souzip.auth.adapter.security.annotation.CurrentUserId;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.wishlist.dto.MyWishlistListResponse;
import com.souzip.domain.wishlist.dto.MyWishlistResponse;
import com.souzip.domain.wishlist.dto.WishlistResponse;
import com.souzip.domain.wishlist.service.WishlistService;
import com.souzip.shared.common.dto.pagination.PaginationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WishlistControllerTest extends RestDocsSupport {

    private final WishlistService wishlistService = mock(WishlistService.class);

    @Override
    protected Object initController() {
        return new WishlistController(wishlistService);
    }

    @BeforeEach
    void setUpWithResolver(RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setCustomArgumentResolvers(
                        new HandlerMethodArgumentResolver() {
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
                        },
                        new PageableHandlerMethodArgumentResolver()
                )
                .apply(documentationConfiguration(provider))
                .build();
    }

    private MyWishlistListResponse createMockMyWishlistListResponse(
            List<MyWishlistResponse> content,
            int currentPage, int totalPages, long totalItems, int pageSize,
            boolean first, boolean last, boolean hasNext, boolean hasPrevious
    ) {
        MyWishlistListResponse response = mock(MyWishlistListResponse.class);
        PaginationResponse.PageInfo pageInfo = mock(PaginationResponse.PageInfo.class);

        given(response.content()).willReturn(content);
        given(response.pagination()).willReturn(pageInfo);
        given(pageInfo.getCurrentPage()).willReturn(currentPage);
        given(pageInfo.getTotalPages()).willReturn(totalPages);
        given(pageInfo.getTotalItems()).willReturn(totalItems);
        given(pageInfo.getPageSize()).willReturn(pageSize);
        given(pageInfo.isFirst()).willReturn(first);
        given(pageInfo.isLast()).willReturn(last);
        given(pageInfo.isHasNext()).willReturn(hasNext);
        given(pageInfo.isHasPrevious()).willReturn(hasPrevious);

        return response;
    }

    @DisplayName("찜 등록")
    @Test
    void addWishlist() throws Exception {
        Long souvenirId = 1L;

        given(wishlistService.addWishlist(1L, souvenirId))
                .willReturn(WishlistResponse.of(souvenirId, true));

        mockMvc.perform(post("/api/wishlists/{souvenirId}", souvenirId)
                        .header("Authorization", "Bearer valid_access_token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.souvenirId").value(souvenirId))
                .andExpect(jsonPath("$.data.wishlisted").value(true))
                .andDo(document("wishlists/add",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("souvenirId").description("찜할 기념품 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.souvenirId").type(JsonFieldType.NUMBER).description("기념품 ID"),
                                fieldWithPath("data.wishlisted").type(JsonFieldType.BOOLEAN).description("찜 여부 (true: 찜 등록됨)"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("찜 취소")
    @Test
    void removeWishlist() throws Exception {
        Long souvenirId = 1L;

        given(wishlistService.removeWishlist(1L, souvenirId))
                .willReturn(WishlistResponse.of(souvenirId, false));

        mockMvc.perform(delete("/api/wishlists/{souvenirId}", souvenirId)
                        .header("Authorization", "Bearer valid_access_token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.souvenirId").value(souvenirId))
                .andExpect(jsonPath("$.data.wishlisted").value(false))
                .andDo(document("wishlists/remove",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("souvenirId").description("찜 취소할 기념품 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.souvenirId").type(JsonFieldType.NUMBER).description("기념품 ID"),
                                fieldWithPath("data.wishlisted").type(JsonFieldType.BOOLEAN).description("찜 여부 (false: 찜 취소됨)"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("내가 찜한 기념품 목록을 조회한다.")
    void getMyWishlists_success() throws Exception {
        List<MyWishlistResponse> content = List.of(
                new MyWishlistResponse(
                        1L,
                        "글로시에 립밤",
                        "US",
                        "https://example.com/image1.jpg",
                        LocalDateTime.of(2024, 1, 15, 10, 30),
                        true
                ),
                new MyWishlistResponse(
                        2L,
                        "말차초콜릿",
                        "JP",
                        "https://example.com/image2.jpg",
                        LocalDateTime.of(2024, 1, 10, 9, 0),
                        true
                )
        );

        MyWishlistListResponse response = createMockMyWishlistListResponse(
                content, 1, 1, 2L, 12, true, true, false, false
        );

        given(wishlistService.getMyWishlist(any(), any())).willReturn(response);

        mockMvc.perform(get("/api/users/me/wishlists")
                        .header("Authorization", "Bearer valid_access_token")
                        .param("page", "0")
                        .param("size", "12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("user/my-wishlists",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (기본값: 0)").optional(),
                                parameterWithName("size").description("페이지 크기 (기본값: 12)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("찜 목록 응답"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("찜한 기념품 목록"),
                                fieldWithPath("data.content[].souvenirId").type(JsonFieldType.NUMBER).description("기념품 ID"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("기념품명"),
                                fieldWithPath("data.content[].countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data.content[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL").optional(),
                                fieldWithPath("data.content[].wishedAt").type(JsonFieldType.STRING).description("찜한 시간"),
                                fieldWithPath("data.content[].isWishlisted").type(JsonFieldType.BOOLEAN).description("찜 여부"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 찜 개수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("찜한 기념품 목록 조회 시 페이지 번호를 지정할 수 있다.")
    void getMyWishlists_withPage() throws Exception {
        List<MyWishlistResponse> content = List.of(
                new MyWishlistResponse(
                        3L,
                        "전통 부채",
                        "KR",
                        null,
                        LocalDateTime.of(2024, 1, 5, 10, 0),
                        true
                )
        );

        MyWishlistListResponse response = createMockMyWishlistListResponse(
                content, 2, 3, 25L, 12, false, false, true, true
        );

        given(wishlistService.getMyWishlist(any(), any())).willReturn(response);

        mockMvc.perform(get("/api/users/me/wishlists")
                        .header("Authorization", "Bearer valid_access_token")
                        .param("page", "1")
                        .param("size", "12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("user/my-wishlists-page-2",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 크기")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("찜 목록 응답"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("찜한 기념품 목록"),
                                fieldWithPath("data.content[].souvenirId").type(JsonFieldType.NUMBER).description("기념품 ID"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("기념품명"),
                                fieldWithPath("data.content[].countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data.content[].thumbnailUrl").type(JsonFieldType.NULL).description("썸네일 이미지 URL").optional(),
                                fieldWithPath("data.content[].wishedAt").type(JsonFieldType.STRING).description("찜한 시간"),
                                fieldWithPath("data.content[].isWishlisted").type(JsonFieldType.BOOLEAN).description("찜 여부"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 찜 개수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("찜한 기념품이 없으면 빈 목록을 반환한다.")
    void getMyWishlists_empty() throws Exception {
        MyWishlistListResponse response = createMockMyWishlistListResponse(
                List.of(), 1, 0, 0L, 12, true, true, false, false
        );

        given(wishlistService.getMyWishlist(any(), any())).willReturn(response);

        mockMvc.perform(get("/api/users/me/wishlists")
                        .header("Authorization", "Bearer valid_access_token")
                        .param("page", "0")
                        .param("size", "12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("user/my-wishlists-empty",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("찜 목록 응답"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("빈 찜 목록"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 찜 개수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }
}
