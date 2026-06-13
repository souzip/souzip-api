package com.souzip.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.souzip.application.notification.FcmTokenCommandService;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.notification.DeviceType;
import com.souzip.domain.notification.FcmToken;
import com.souzip.domain.notification.FcmTokenRegisterRequest;
import com.souzip.domain.notification.dto.FcmTokenResponse;
import com.souzip.shared.exception.GlobalExceptionHandler;
import com.souzip.auth.adapter.security.annotation.CurrentUserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FcmTokenControllerTest extends RestDocsSupport {

    private final FcmTokenCommandService fcmTokenCommandService = mock(FcmTokenCommandService.class);

    @Override
    protected Object initController() {
        return new FcmTokenController(fcmTokenCommandService);
    }

    @BeforeEach
    void setUpWithUserIdResolver(RestDocumentationContextProvider provider) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .setControllerAdvice(new GlobalExceptionHandler())
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

    @DisplayName("FCM 토큰을 등록할 수 있다")
    @Test
    void register() throws Exception {
        // given
        FcmToken token = createToken("fcm-token-123", "device-id-123");
        ReflectionTestUtils.setField(token, "id", 10L);

        Map<String, Object> requestBody = Map.of(
                "fcmToken", "fcm-token-123",
                "deviceType", "ANDROID",
                "deviceId", "device-id-123",
                "deviceModel", "Galaxy S23",
                "osVersion", "Android 14",
                "appVersion", "1.0.0"
        );

        given(fcmTokenCommandService.registerOrUpdate(eq(1L), any(FcmTokenRegisterRequest.class)))
                .willReturn(token);

        // when & then
        mockMvc.perform(post("/api/users/me/fcm-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.deviceId").value("device-id-123"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.message").value("FCM 토큰이 등록되었습니다."))
                .andDo(document("fcm-token/register",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("fcmToken").type(JsonFieldType.STRING).description("FCM 디바이스 토큰"),
                                fieldWithPath("deviceType").type(JsonFieldType.STRING).description("디바이스 타입 (ANDROID, IOS)"),
                                fieldWithPath("deviceId").type(JsonFieldType.STRING).description("디바이스 고유 ID"),
                                fieldWithPath("deviceModel").type(JsonFieldType.STRING).description("디바이스 모델명").optional(),
                                fieldWithPath("osVersion").type(JsonFieldType.STRING).description("OS 버전").optional(),
                                fieldWithPath("appVersion").type(JsonFieldType.STRING).description("앱 버전").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("FCM 토큰 ID"),
                                fieldWithPath("data.deviceId").type(JsonFieldType.STRING).description("디바이스 ID"),
                                fieldWithPath("data.active").type(JsonFieldType.BOOLEAN).description("활성화 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("FCM 토큰을 비활성화할 수 있다")
    @Test
    void deactivate() throws Exception {
        // given
        willDoNothing().given(fcmTokenCommandService).deactivateByDevice(1L, "device-id-123");

        // when & then
        mockMvc.perform(delete("/api/users/me/fcm-tokens?deviceId={deviceId}", "device-id-123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("FCM 토큰이 비활성화되었습니다."))
                .andDo(document("fcm-token/deactivate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("deviceId").description("비활성화할 디바이스 ID")
                        ),
                        responseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    private FcmToken createToken(String fcmToken, String deviceId) {
        FcmTokenRegisterRequest request = FcmTokenRegisterRequest.of(
                fcmToken, DeviceType.ANDROID, deviceId, "Galaxy S23", "Android 14", "1.0.0"
        );
        FcmToken token = FcmToken.register(request);
        token.linkUser(1L);
        return token;
    }
}
