package com.souzip.api.domain.user.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.dto.OnboardingResponse;
import com.souzip.api.domain.user.service.UserService;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static com.souzip.api.docs.CommonDocumentation.errorResponseFields;
import static com.souzip.api.docs.CommonDocumentation.validationErrorResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends RestDocsSupport {

    private final UserService userService = mock(UserService.class);

    @Override
    protected Object initController() {
        return new UserController(userService);
    }

    @Test
    @DisplayName("온보딩을 완료한다.")
    void completeOnboarding_success() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            "수집왕",
            "https://cdn.souzip.com/characters/character1.png",
            List.of("FOOD_SNACK", "BEAUTY_HEALTH", "FASHION_ACCESSORY")
        );

        List<CategoryDto> categoryDtos = List.of(
            new CategoryDto("FOOD_SNACK", "먹거리·간식"),
            new CategoryDto("BEAUTY_HEALTH", "뷰티·헬스"),
            new CategoryDto("FASHION_ACCESSORY", "패션·악세서리")
        );

        OnboardingResponse response = new OnboardingResponse(
            "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            "수집왕",
            "https://cdn.souzip.com/characters/character1.png",
            "test@gmail.com",
            categoryDtos
        );

        given(userService.completeOnboarding(any(), any(OnboardingRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/users/onboarding")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            .andExpect(jsonPath("$.data.nickname").value("수집왕"))
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://cdn.souzip.com/characters/character1.png"))
            .andExpect(jsonPath("$.data.email").value("test@gmail.com"))
            .andExpect(jsonPath("$.data.categories").isArray())
            .andExpect(jsonPath("$.data.categories.length()").value(3))
            .andExpect(jsonPath("$.data.categories[0].name").value("FOOD_SNACK"))
            .andExpect(jsonPath("$.data.categories[0].label").value("먹거리·간식"))
            .andExpect(jsonPath("$.message").value(""))
            .andDo(document("user/onboarding-success",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임 (최대 15자)"),
                    fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                        .description("프로필 이미지 URL (캐릭터 이미지)"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록 (최소 1개, Category ENUM name)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("온보딩 완료 응답 데이터"),
                    fieldWithPath("data.userId").type(JsonFieldType.STRING)
                        .description("사용자 ID (UUID)"),
                    fieldWithPath("data.nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("data.profileImageUrl").type(JsonFieldType.STRING)
                        .description("프로필 이미지 URL"),
                    fieldWithPath("data.email").type(JsonFieldType.STRING)
                        .description("사용자 이메일"),
                    fieldWithPath("data.categories").type(JsonFieldType.ARRAY)
                        .description("선택한 카테고리 목록"),
                    fieldWithPath("data.categories[].name").type(JsonFieldType.STRING)
                        .description("카테고리 ENUM name"),
                    fieldWithPath("data.categories[].label").type(JsonFieldType.STRING)
                        .description("카테고리 한글 라벨"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("성공 메시지")
                )
            ));
    }

    @Test
    @DisplayName("이미 온보딩을 완료한 사용자는 온보딩을 다시 할 수 없다.")
    void completeOnboarding_alreadyCompleted() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            "수집왕",
            "https://cdn.souzip.com/characters/character1.png",
            List.of("FOOD_SNACK", "BEAUTY_HEALTH")
        );

        given(userService.completeOnboarding(any(), any(OnboardingRequest.class)))
            .willThrow(new BusinessException(ErrorCode.ONBOARDING_ALREADY_COMPLETED));

        // when & then
        mockMvc.perform(post("/api/users/onboarding")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이미 온보딩을 완료한 사용자입니다."))
            .andDo(document("user/onboarding-already-completed",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                        .description("프로필 이미지 URL"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록")
                ),
                responseFields(errorResponseFields())  // ← Business Exception
            ));
    }

    @Test
    @DisplayName("유효하지 않은 카테고리로 온보딩 시 에러가 발생한다.")
    void completeOnboarding_invalidCategory() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            "수집왕",
            "https://cdn.souzip.com/characters/character1.png",
            List.of("INVALID_CATEGORY", "BEAUTY_HEALTH")
        );

        given(userService.completeOnboarding(any(), any(OnboardingRequest.class)))
            .willThrow(new BusinessException(ErrorCode.INVALID_CATEGORY));

        // when & then
        mockMvc.perform(post("/api/users/onboarding")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("유효하지 않은 카테고리입니다."))
            .andDo(document("user/onboarding-invalid-category",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                        .description("프로필 이미지 URL"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록 (유효하지 않은 카테고리 포함)")
                ),
                responseFields(errorResponseFields())  // ← Business Exception
            ));
    }

    @Test
    @DisplayName("닉네임이 비어있으면 400 에러가 발생한다.")
    void completeOnboarding_emptyNickname() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            "",
            "https://cdn.souzip.com/characters/character1.png",
            List.of("FOOD_SNACK")
        );

        // when & then
        mockMvc.perform(post("/api/users/onboarding")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andDo(document("user/onboarding-empty-nickname",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("빈 닉네임"),
                    fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                        .description("프로필 이미지 URL"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록")
                ),
                responseFields(validationErrorResponseFields())  // ← Validation Error!
            ));
    }

    @Test
    @DisplayName("닉네임이 15자를 초과하면 400 에러가 발생한다.")
    void completeOnboarding_nicknameTooLong() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            "이것은15자를초과하는닉네임입니다",  // 16자
            "https://cdn.souzip.com/characters/character1.png",
            List.of("FOOD_SNACK")
        );

        // when & then
        mockMvc.perform(post("/api/users/onboarding")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andDo(document("user/onboarding-nickname-too-long",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("15자를 초과하는 닉네임"),
                    fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                        .description("프로필 이미지 URL"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록")
                ),
                responseFields(validationErrorResponseFields())  // ← Validation Error!
            ));
    }

    @Test
    @DisplayName("카테고리가 비어있으면 400 에러가 발생한다.")
    void completeOnboarding_emptyCategories() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            "수집왕",
            "https://cdn.souzip.com/characters/character1.png",
            List.of()
        );

        // when & then
        mockMvc.perform(post("/api/users/onboarding")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andDo(document("user/onboarding-empty-categories",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                        .description("프로필 이미지 URL"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("빈 카테고리 목록")
                ),
                responseFields(validationErrorResponseFields())  // ← Validation Error!
            ));
    }

    @Test
    @DisplayName("회원탈퇴를 한다.")
    void withdraw_success() throws Exception {
        // given
        doNothing().when(userService).withdraw(any());

        // when & then
        mockMvc.perform(delete("/api/users/me")
                .header("Authorization", "Bearer valid_access_token"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("회원탈퇴가 완료되었습니다."))
            .andDo(document("user/withdraw",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.NULL)
                        .description("응답 데이터 (null)"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("성공 메시지")
                )
            ));
    }
}
