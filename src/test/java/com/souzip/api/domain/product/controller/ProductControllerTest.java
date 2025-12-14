package com.souzip.api.domain.product.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.product.dto.ProductCreateRequestDto;
import com.souzip.api.domain.product.dto.ProductResponseDto;
import com.souzip.api.domain.product.dto.ProductUpdateRequestDto;
import com.souzip.api.domain.product.entity.Category;
import com.souzip.api.domain.product.entity.Purpose;
import com.souzip.api.domain.product.service.ProductService;
import com.souzip.api.global.security.annotation.CurrentUserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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

class ProductControllerTest extends RestDocsSupport {

    private final ProductService productService = org.mockito.Mockito.mock(ProductService.class);

    @Override
    protected Object initController() {
        return new ProductController(productService);
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
    @DisplayName("상품 생성")
    void createProductWithFiles() throws Exception {

        ProductCreateRequestDto request = new ProductCreateRequestDto(
                "테스트 기념품",
                10000,
                "테스트 설명",
                Category.SOUVENIR_BASIC,
                Purpose.GIFT,
                1L,
                Collections.emptyList()
        );

        MockMultipartFile productPart = new MockMultipartFile(
                "product",
                "product.json",
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

        ProductResponseDto response = new ProductResponseDto(
                1L,
                "테스트 기념품",
                10000,
                "테스트 설명",
                Category.SOUVENIR_BASIC,
                Purpose.GIFT,
                1L,
                filesResponse
        );

        given(productService.createProduct(
                any(ProductCreateRequestDto.class),
                eq(1L),
                any(List.class)
        )).willReturn(response);

        mockMvc.perform(multipart("/api/products")
                        .file(productPart)
                        .file(file1)
                        .file(file2)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.files[0].originalName").value("file1.jpg"))
                .andExpect(jsonPath("$.data.files[1].originalName").value("file2.jpg"))
                .andDo(document("products/create-product-with-files",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("상품 ID"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("상품명"),
                                fieldWithPath("data.price").type(JsonFieldType.NUMBER).description("가격"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("상세 설명"),
                                fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리"),
                                fieldWithPath("data.purpose").type(JsonFieldType.STRING).description("사용 목적"),
                                fieldWithPath("data.cityId").type(JsonFieldType.NUMBER).description("도시 ID"),
                                fieldWithPath("data.files").type(JsonFieldType.ARRAY).description("업로드된 파일 리스트"),
                                fieldWithPath("data.files[].id").type(JsonFieldType.NUMBER).description("파일 ID"),
                                fieldWithPath("data.files[].url").type(JsonFieldType.STRING).description("파일 URL"),
                                fieldWithPath("data.files[].originalName").type(JsonFieldType.STRING).description("원본 파일명"),
                                fieldWithPath("data.files[].displayOrder").type(JsonFieldType.NUMBER).description("파일 순서"),
                                fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("상품 수정")
    void updateProductWithFiles() throws Exception {
        Long productId = 1L;

        ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(
                "업데이트 기념품",
                20000,
                "업데이트 설명",
                Category.SOUVENIR_BASIC,
                Purpose.PERSONAL,
                1L,
                Collections.emptyList()
        );

        MockMultipartFile productPart = new MockMultipartFile(
                "product",
                "product.json",
                "application/json",
                objectMapper.writeValueAsBytes(requestDto)
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

        ProductResponseDto response = new ProductResponseDto(
                productId,
                requestDto.name(),
                requestDto.price(),
                requestDto.description(),
                requestDto.category(),
                requestDto.purpose(),
                requestDto.cityId(),
                filesResponse
        );

        given(productService.updateProduct(
                eq(productId),
                any(ProductUpdateRequestDto.class),
                eq(1L),
                any(List.class)
        )).willReturn(response);

        mockMvc.perform(multipart("/api/products/{id}", productId)
                        .file(productPart)
                        .file(file1)
                        .file(file2)
                        .with(request -> { request.setMethod("PUT"); return request; })
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.files[0].originalName").value("file1.jpg"))
                .andExpect(jsonPath("$.data.files[1].originalName").value("file2.jpg"))
                .andDo(document("products/update-product-with-files",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("상품 ID"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("상품명"),
                                fieldWithPath("data.price").type(JsonFieldType.NUMBER).description("가격"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("상세 설명"),
                                fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리"),
                                fieldWithPath("data.purpose").type(JsonFieldType.STRING).description("사용 목적"),
                                fieldWithPath("data.cityId").type(JsonFieldType.NUMBER).description("도시 ID"),
                                fieldWithPath("data.files").type(JsonFieldType.ARRAY).description("업로드된 파일 리스트"),
                                fieldWithPath("data.files[].id").type(JsonFieldType.NUMBER).description("파일 ID"),
                                fieldWithPath("data.files[].url").type(JsonFieldType.STRING).description("파일 URL"),
                                fieldWithPath("data.files[].originalName").type(JsonFieldType.STRING).description("원본 파일명"),
                                fieldWithPath("data.files[].displayOrder").type(JsonFieldType.NUMBER).description("파일 순서"),
                                fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("상품 삭제")
    void deleteProduct() throws Exception {
        Long productId = 1L;

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("기념품이 성공적으로 삭제되었습니다."))
                .andDo(document("products/delete-product",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).optional().description("삭제된 데이터는 null"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }
}
