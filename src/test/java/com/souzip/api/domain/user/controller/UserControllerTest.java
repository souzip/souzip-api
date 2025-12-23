package com.souzip.api.domain.user.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.user.dto.NicknameCheckRequest;
import com.souzip.api.domain.user.dto.NicknameCheckResponse;
import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.dto.OnboardingResponse;
import com.souzip.api.domain.user.dto.UserAgreementInfo;
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
    @DisplayName("사용 가능한 닉네임을 확인한다.")
    void checkNickname_available() throws Exception {
        // given
        NicknameCheckRequest request = new NicknameCheckRequest("새로운닉네임");
        NicknameCheckResponse response = NicknameCheckResponse.available();

        given(userService.checkNickname("새로운닉네임")).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/users/check-nickname")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.available").value(true))
            .andExpect(jsonPath("$.data.message").value("사용 가능한 닉네임입니다."))
            .andDo(document("user/check-nickname-available",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("확인할 닉네임 (2~11자, 한글/영문/숫자만 가능)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("닉네임 중복 확인 결과"),
                    fieldWithPath("data.available").type(JsonFieldType.BOOLEAN)
                        .description("사용 가능 여부 (true: 사용 가능, false: 사용 불가)"),
                    fieldWithPath("data.message").type(JsonFieldType.STRING)
                        .description("확인 결과 메시지"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지")
                )
            ));
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임을 확인한다.")
    void checkNickname_unavailable() throws Exception {
        // given
        NicknameCheckRequest request = new NicknameCheckRequest("중복닉네임");
        NicknameCheckResponse response = NicknameCheckResponse.unavailable();

        given(userService.checkNickname("중복닉네임")).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/users/check-nickname")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.available").value(false))
            .andExpect(jsonPath("$.data.message").value("이미 사용 중인 닉네임입니다."))
            .andDo(document("user/check-nickname-unavailable",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("확인할 닉네임 (이미 사용 중)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("닉네임 중복 확인 결과"),
                    fieldWithPath("data.available").type(JsonFieldType.BOOLEAN)
                        .description("사용 가능 여부 (false: 중복됨)"),
                    fieldWithPath("data.message").type(JsonFieldType.STRING)
                        .description("확인 결과 메시지"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지")
                )
            ));
    }

    @Test
    @DisplayName("온보딩 시 닉네임이 중복되면 409 에러가 발생한다.")
    void completeOnboarding_nicknameAlreadyExists() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            true, true, true, true, false,
            "중복닉네임",
            "red",
            List.of("FOOD_SNACK")
        );

        given(userService.completeOnboarding(any(), any(OnboardingRequest.class)))
            .willThrow(new BusinessException(ErrorCode.NICKNAME_DUPLICATED));

        // when & then
        mockMvc.perform(post("/api/users/onboarding")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."))
            .andDo(document("user/onboarding-nickname-duplicated",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("ageVerified").type(JsonFieldType.BOOLEAN)
                        .description("만 14세 이상 여부"),
                    fieldWithPath("serviceTerms").type(JsonFieldType.BOOLEAN)
                        .description("서비스 이용약관 동의"),
                    fieldWithPath("privacyRequired").type(JsonFieldType.BOOLEAN)
                        .description("개인정보 수집 및 이용 동의"),
                    fieldWithPath("locationService").type(JsonFieldType.BOOLEAN)
                        .description("위치기반 서비스 이용약관 동의"),
                    fieldWithPath("marketingConsent").type(JsonFieldType.BOOLEAN)
                        .description("마케팅 수신 동의"),
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("중복된 닉네임"),
                    fieldWithPath("profileImageColor").type(JsonFieldType.STRING)
                        .description("프로필 이미지 색상"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록")
                ),
                responseFields(errorResponseFields())
            ));
    }

    @Test
    @DisplayName("온보딩을 완료한다.")
    void completeOnboarding_success() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            true,
            true,
            true,
            true,
            false,
            "수집",
            "red",
            List.of("FOOD_SNACK", "BEAUTY_HEALTH", "FASHION_ACCESSORY")
        );

        List<CategoryDto> categoryDtos = List.of(
            new CategoryDto("FOOD_SNACK", "먹거리·간식"),
            new CategoryDto("BEAUTY_HEALTH", "뷰티·헬스"),
            new CategoryDto("FASHION_ACCESSORY", "패션·악세서리")
        );

        UserAgreementInfo agreementInfo =
            new UserAgreementInfo(true, true, true, true, false);

        OnboardingResponse response = new OnboardingResponse(
            "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            "수집",
            "https://kr.object.ncloudstorage.com/souzip-dev-images/profile/red.svg",
            categoryDtos,
            agreementInfo
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
            .andExpect(jsonPath("$.data.nickname").value("수집"))
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://kr.object.ncloudstorage.com/souzip-dev-images/profile/red.svg"))
            .andExpect(jsonPath("$.data.categories").isArray())
            .andExpect(jsonPath("$.data.categories.length()").value(3))
            .andExpect(jsonPath("$.data.categories[0].name").value("FOOD_SNACK"))
            .andExpect(jsonPath("$.data.categories[0].label").value("먹거리·간식"))
            .andExpect(jsonPath("$.data.agreements.ageVerified").value(true))
            .andExpect(jsonPath("$.data.agreements.marketingConsent").value(false))
            .andExpect(jsonPath("$.message").value(""))
            .andDo(document("user/onboarding-success",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("ageVerified").type(JsonFieldType.BOOLEAN)
                        .description("만 14세 이상 여부 (필수)"),
                    fieldWithPath("serviceTerms").type(JsonFieldType.BOOLEAN)
                        .description("서비스 이용약관 동의 (필수)"),
                    fieldWithPath("privacyRequired").type(JsonFieldType.BOOLEAN)
                        .description("개인정보 수집 및 이용 동의 (필수)"),
                    fieldWithPath("locationService").type(JsonFieldType.BOOLEAN)
                        .description("위치기반 서비스 이용약관 동의 (필수)"),
                    fieldWithPath("marketingConsent").type(JsonFieldType.BOOLEAN)
                        .description("마케팅 수신 동의 (선택)"),
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임 (최대 11자)"),
                    fieldWithPath("profileImageColor").type(JsonFieldType.STRING)
                        .description("프로필 이미지 색상 (red, blue, yellow, purple)"),
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
                        .description("프로필 이미지 URL (NCP Object Storage 공개 URL)"),
                    fieldWithPath("data.categories").type(JsonFieldType.ARRAY)
                        .description("선택한 카테고리 목록"),
                    fieldWithPath("data.categories[].name").type(JsonFieldType.STRING)
                        .description("카테고리 ENUM name"),
                    fieldWithPath("data.categories[].label").type(JsonFieldType.STRING)
                        .description("카테고리 한글 라벨"),
                    fieldWithPath("data.agreements").type(JsonFieldType.OBJECT)
                        .description("약관 동의 정보"),
                    fieldWithPath("data.agreements.ageVerified").type(JsonFieldType.BOOLEAN)
                        .description("만 14세 이상 여부"),
                    fieldWithPath("data.agreements.serviceTerms").type(JsonFieldType.BOOLEAN)
                        .description("서비스 이용약관 동의"),
                    fieldWithPath("data.agreements.privacyRequired").type(JsonFieldType.BOOLEAN)
                        .description("개인정보 수집 및 이용 동의"),
                    fieldWithPath("data.agreements.locationService").type(JsonFieldType.BOOLEAN)
                        .description("위치기반 서비스 이용약관 동의"),
                    fieldWithPath("data.agreements.marketingConsent").type(JsonFieldType.BOOLEAN)
                        .description("마케팅 수신 동의"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("성공 메시지")
                )
            ));
    }

    @Test
    @DisplayName("필수 약관에 동의하지 않으면 400 에러가 발생한다.")
    void completeOnboarding_requiredAgreementNotChecked() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            true,
            false,
            true,
            true,
            false,
            "수집",
            "red",
            List.of("FOOD_SNACK")
        );

        given(userService.completeOnboarding(any(), any(OnboardingRequest.class)))
            .willThrow(new BusinessException(ErrorCode.INVALID_INPUT, "필수 약관에 모두 동의해야 합니다."));

        // when & then
        mockMvc.perform(post("/api/users/onboarding")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("필수 약관에 모두 동의해야 합니다."))
            .andDo(document("user/onboarding-required-agreement-not-checked",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("ageVerified").type(JsonFieldType.BOOLEAN)
                        .description("만 14세 이상 여부"),
                    fieldWithPath("serviceTerms").type(JsonFieldType.BOOLEAN)
                        .description("서비스 이용약관 동의 (false)"),
                    fieldWithPath("privacyRequired").type(JsonFieldType.BOOLEAN)
                        .description("개인정보 수집 및 이용 동의"),
                    fieldWithPath("locationService").type(JsonFieldType.BOOLEAN)
                        .description("위치기반 서비스 이용약관 동의"),
                    fieldWithPath("marketingConsent").type(JsonFieldType.BOOLEAN)
                        .description("마케팅 수신 동의"),
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("profileImageColor").type(JsonFieldType.STRING)
                        .description("프로필 이미지 색상"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록")
                ),
                responseFields(errorResponseFields())
            ));
    }

    @Test
    @DisplayName("이미 온보딩을 완료한 사용자는 온보딩을 다시 할 수 없다.")
    void completeOnboarding_alreadyCompleted() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            true, true, true, true, false,
            "수집",
            "blue",
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
                    fieldWithPath("ageVerified").type(JsonFieldType.BOOLEAN)
                        .description("만 14세 이상 여부"),
                    fieldWithPath("serviceTerms").type(JsonFieldType.BOOLEAN)
                        .description("서비스 이용약관 동의"),
                    fieldWithPath("privacyRequired").type(JsonFieldType.BOOLEAN)
                        .description("개인정보 수집 및 이용 동의"),
                    fieldWithPath("locationService").type(JsonFieldType.BOOLEAN)
                        .description("위치기반 서비스 이용약관 동의"),
                    fieldWithPath("marketingConsent").type(JsonFieldType.BOOLEAN)
                        .description("마케팅 수신 동의"),
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("profileImageColor").type(JsonFieldType.STRING)
                        .description("프로필 이미지 색상"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록")
                ),
                responseFields(errorResponseFields())
            ));
    }

    @Test
    @DisplayName("유효하지 않은 카테고리로 온보딩 시 에러가 발생한다.")
    void completeOnboarding_invalidCategory() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            true, true, true, true, false,
            "수집",
            "yellow",
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
                    fieldWithPath("ageVerified").type(JsonFieldType.BOOLEAN)
                        .description("만 14세 이상 여부"),
                    fieldWithPath("serviceTerms").type(JsonFieldType.BOOLEAN)
                        .description("서비스 이용약관 동의"),
                    fieldWithPath("privacyRequired").type(JsonFieldType.BOOLEAN)
                        .description("개인정보 수집 및 이용 동의"),
                    fieldWithPath("locationService").type(JsonFieldType.BOOLEAN)
                        .description("위치기반 서비스 이용약관 동의"),
                    fieldWithPath("marketingConsent").type(JsonFieldType.BOOLEAN)
                        .description("마케팅 수신 동의"),
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("profileImageColor").type(JsonFieldType.STRING)
                        .description("프로필 이미지 색상"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록 (유효하지 않은 카테고리 포함)")
                ),
                responseFields(errorResponseFields())
            ));
    }

    @Test
    @DisplayName("유효하지 않은 프로필 색상으로 온보딩 시 에러가 발생한다.")
    void completeOnboarding_invalidProfileColor() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(
            true, true, true, true, false,
            "수집",
            "invalid_color",
            List.of("FOOD_SNACK")
        );

        given(userService.completeOnboarding(any(), any(OnboardingRequest.class)))
            .willThrow(new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE_COLOR));

        // when & then
        mockMvc.perform(post("/api/users/onboarding")
                .header("Authorization", "Bearer valid_access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("유효하지 않은 프로필 이미지 색상입니다."))
            .andDo(document("user/onboarding-invalid-profile-color",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("ageVerified").type(JsonFieldType.BOOLEAN)
                        .description("만 14세 이상 여부"),
                    fieldWithPath("serviceTerms").type(JsonFieldType.BOOLEAN)
                        .description("서비스 이용약관 동의"),
                    fieldWithPath("privacyRequired").type(JsonFieldType.BOOLEAN)
                        .description("개인정보 수집 및 이용 동의"),
                    fieldWithPath("locationService").type(JsonFieldType.BOOLEAN)
                        .description("위치기반 서비스 이용약관 동의"),
                    fieldWithPath("marketingConsent").type(JsonFieldType.BOOLEAN)
                        .description("마케팅 수신 동의"),
                    fieldWithPath("nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("profileImageColor").type(JsonFieldType.STRING)
                        .description("유효하지 않은 프로필 이미지 색상"),
                    fieldWithPath("categories").type(JsonFieldType.ARRAY)
                        .description("관심 카테고리 목록")
                ),
                responseFields(errorResponseFields())
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
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("성공 메시지")
                )
            ));
    }
}
