package com.souzip.api.domain.user.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.souvenir.dto.MySouvenirListResponse;
import com.souzip.api.domain.souvenir.dto.MySouvenirResponse;
import com.souzip.api.domain.user.dto.NicknameCheckRequest;
import com.souzip.api.domain.user.dto.NicknameCheckResponse;
import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.dto.OnboardingResponse;
import com.souzip.api.domain.user.dto.UserAgreementInfo;
import com.souzip.api.domain.user.dto.UserProfileResponse;
import com.souzip.api.domain.user.service.UserService;
import com.souzip.api.global.common.dto.pagination.PaginationResponse;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDateTime;
import java.util.List;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static com.souzip.api.docs.CommonDocumentation.errorResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends RestDocsSupport {

    private final UserService userService = mock(UserService.class);

    @Override
    protected Object initController() {
        return new UserController(userService);
    }

    // Mock 헬퍼 메서드
    private MySouvenirListResponse createMockMySouvenirListResponse(
        List<MySouvenirResponse> content,
        int currentPage, int totalPages, long totalItems, int pageSize,
        boolean first, boolean last, boolean hasNext, boolean hasPrevious
    ) {
        MySouvenirListResponse response = mock(MySouvenirListResponse.class);
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

    @Test
    @DisplayName("내 프로필을 조회한다.")
    void getMyProfile_success() throws Exception {
        // given
        UserProfileResponse response = new UserProfileResponse(
            "a1b2c3d4...",
            "테스트닉네임",
            "test@kakao.com",
            "https://example.com/profile.jpg"
        );

        given(userService.getUserProfile(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer valid_access_token"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value("a1b2c3d4..."))
            .andExpect(jsonPath("$.data.nickname").value("테스트닉네임"))
            .andExpect(jsonPath("$.data.email").value("test@kakao.com"))
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.jpg"))
            .andDo(document("user/my-profile",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("사용자 프로필 정보"),
                    fieldWithPath("data.userId").type(JsonFieldType.STRING)
                        .description("사용자 ID"),
                    fieldWithPath("data.nickname").type(JsonFieldType.STRING)
                        .description("닉네임"),
                    fieldWithPath("data.email").type(JsonFieldType.STRING)
                        .description("이메일"),
                    fieldWithPath("data.profileImageUrl").type(JsonFieldType.STRING)
                        .description("프로필 이미지 URL"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지")
                )
            ));
    }

    @Test
    @DisplayName("내가 등록한 기념품 목록을 조회한다.")
    void getMySouvenirs_success() throws Exception {
        // given
        List<MySouvenirResponse> content = List.of(
            new MySouvenirResponse(
                1L,
                "https://example.com/image1.jpg",
                "KR",
                LocalDateTime.of(2024, 1, 15, 10, 30),
                LocalDateTime.of(2024, 1, 20, 15, 0)
            ),
            new MySouvenirResponse(
                2L,
                "https://example.com/image2.jpg",
                "FR",
                LocalDateTime.of(2024, 1, 10, 9, 0),
                LocalDateTime.of(2024, 1, 10, 9, 0)
            )
        );

        MySouvenirListResponse response = createMockMySouvenirListResponse(
            content, 1, 1, 2L, 12, true, true, false, false
        );

        given(userService.getMySouvenirs(any(), eq(1), eq(12))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me/souvenirs")
                .header("Authorization", "Bearer valid_access_token")
                .param("page", "1")
                .param("size", "12"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.content[0].id").value(1))
            .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value("https://example.com/image1.jpg"))
            .andExpect(jsonPath("$.data.content[0].countryCode").value("KR"))
            .andExpect(jsonPath("$.data.content[0].createdAt").exists())
            .andExpect(jsonPath("$.data.content[0].updatedAt").exists())
            .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
            .andExpect(jsonPath("$.data.pagination.pageSize").value(12))
            .andExpect(jsonPath("$.data.pagination.totalItems").value(2))
            .andExpect(jsonPath("$.data.pagination.totalPages").value(1))
            .andExpect(jsonPath("$.data.pagination.hasNext").value(false))
            .andDo(document("user/my-souvenirs",
                getDocumentRequest(),
                getDocumentResponse(),
                queryParameters(
                    parameterWithName("page").description("페이지 번호 (기본값: 1)").optional(),
                    parameterWithName("size").description("페이지 크기 (기본값: 12)").optional()
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("기념품 목록 응답"),
                    fieldWithPath("data.content").type(JsonFieldType.ARRAY)
                        .description("기념품 목록"),
                    fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER)
                        .description("기념품 ID"),
                    fieldWithPath("data.content[].thumbnailUrl").type(JsonFieldType.STRING)
                        .description("썸네일 이미지 URL"),
                    fieldWithPath("data.content[].countryCode").type(JsonFieldType.STRING)
                        .description("기념품 국가 코드"),
                    fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING)
                        .description("생성일시"),
                    fieldWithPath("data.content[].updatedAt").type(JsonFieldType.STRING)
                        .description("수정일시"),
                    fieldWithPath("data.pagination").type(JsonFieldType.OBJECT)
                        .description("페이지네이션 정보"),
                    fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER)
                        .description("현재 페이지 번호"),
                    fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER)
                        .description("전체 페이지 수"),
                    fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER)
                        .description("전체 기념품 개수"),
                    fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER)
                        .description("페이지 크기"),
                    fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN)
                        .description("첫 번째 페이지 여부"),
                    fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN)
                        .description("마지막 페이지 여부"),
                    fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN)
                        .description("다음 페이지 존재 여부"),
                    fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN)
                        .description("이전 페이지 존재 여부"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지")
                )
            ));
    }

    @Test
    @DisplayName("기념품 목록 조회 시 페이지 번호를 지정할 수 있다.")
    void getMySouvenirs_withPage() throws Exception {
        // given
        List<MySouvenirResponse> content = List.of(
            new MySouvenirResponse(
                3L,
                null,
                "KR",
                LocalDateTime.of(2024, 1, 5, 10, 0),
                LocalDateTime.of(2024, 1, 5, 10, 0)
            )
        );

        MySouvenirListResponse response = createMockMySouvenirListResponse(
                content, 2, 3, 25L, 12, false, false, true, true
        );

        given(userService.getMySouvenirs(any(), eq(2), eq(12))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me/souvenirs")
                .header("Authorization", "Bearer valid_access_token")
                .param("page", "2")
                .param("size", "12"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pagination.currentPage").value(2))
            .andExpect(jsonPath("$.data.pagination.totalPages").value(3))
            .andExpect(jsonPath("$.data.pagination.hasNext").value(true))
            .andDo(document("user/my-souvenirs-page-2",
                getDocumentRequest(),
                getDocumentResponse(),
                queryParameters(
                    parameterWithName("page").description("페이지 번호 (2페이지 조회)"),
                    parameterWithName("size").description("페이지 크기")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("기념품 목록 응답"),
                    fieldWithPath("data.content").type(JsonFieldType.ARRAY)
                        .description("기념품 목록"),
                    fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER)
                        .description("기념품 ID"),
                    fieldWithPath("data.content[].thumbnailUrl").type(JsonFieldType.NULL)
                        .description("썸네일 이미지 URL"),
                    fieldWithPath("data.content[].countryCode").type(JsonFieldType.STRING)
                        .description("기념품 국가 코드"),
                    fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING)
                        .description("생성일시"),
                    fieldWithPath("data.content[].updatedAt").type(JsonFieldType.STRING)
                        .description("수정일시"),
                    fieldWithPath("data.pagination").type(JsonFieldType.OBJECT)
                        .description("페이지네이션 정보"),
                    fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER)
                        .description("현재 페이지 번호 (2)"),
                    fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER)
                        .description("전체 페이지 수"),
                    fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER)
                        .description("전체 기념품 개수"),
                    fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER)
                        .description("페이지 크기"),
                    fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN)
                        .description("첫 번째 페이지 여부"),
                    fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN)
                        .description("마지막 페이지 여부"),
                    fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN)
                        .description("다음 페이지 존재 여부"),
                    fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN)
                        .description("이전 페이지 존재 여부"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지")
                )
            ));
    }

    @Test
    @DisplayName("등록한 기념품이 없으면 빈 목록을 반환한다.")
    void getMySouvenirs_empty() throws Exception {
        // given
        MySouvenirListResponse response = createMockMySouvenirListResponse(
            List.of(), 1, 0, 0L, 12, true, true, false, false
        );

        given(userService.getMySouvenirs(any(), eq(1), eq(12))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me/souvenirs")
                .header("Authorization", "Bearer valid_access_token")
                .param("page", "1")
                .param("size", "12"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andExpect(jsonPath("$.data.pagination.totalItems").value(0))
            .andDo(document("user/my-souvenirs-empty",
                getDocumentRequest(),
                getDocumentResponse(),
                queryParameters(
                    parameterWithName("page").description("페이지 번호"),
                    parameterWithName("size").description("페이지 크기")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("기념품 목록 응답"),
                    fieldWithPath("data.content").type(JsonFieldType.ARRAY)
                        .description("빈 기념품 목록"),
                    fieldWithPath("data.pagination").type(JsonFieldType.OBJECT)
                        .description("페이지네이션 정보"),
                    fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER)
                        .description("현재 페이지 번호"),
                    fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER)
                        .description("전체 페이지 수 (0)"),
                    fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER)
                        .description("전체 기념품 개수 (0)"),
                    fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER)
                        .description("페이지 크기"),
                    fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN)
                        .description("첫 번째 페이지 여부"),
                    fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN)
                        .description("마지막 페이지 여부"),
                    fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN)
                        .description("다음 페이지 존재 여부 (false)"),
                    fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN)
                        .description("이전 페이지 존재 여부 (false)"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지")
                )
            ));
    }
}
