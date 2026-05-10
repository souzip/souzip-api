package com.souzip.domain.user.controller;

import com.souzip.auth.adapter.security.annotation.CurrentUserId;
import com.souzip.auth.adapter.security.annotation.RequireAuth;
import com.souzip.domain.souvenir.dto.MySouvenirListResponse;
import com.souzip.domain.user.dto.*;
import com.souzip.domain.user.service.UserService;
import com.souzip.shared.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/check-nickname")
    @RequireAuth
    public SuccessResponse<NicknameCheckResponse> checkNickname(
            @Valid @RequestBody NicknameCheckRequest request
    ) {
        NicknameCheckResponse response = userService.checkNickname(request.nickname());
        return SuccessResponse.of(response);
    }

    @PostMapping("/onboarding")
    @RequireAuth
    public SuccessResponse<OnboardingResponse> completeOnboarding(
            @CurrentUserId Long currentUserId,
            @Valid @RequestBody OnboardingRequest request
    ) {
        OnboardingResponse response = userService.completeOnboarding(currentUserId, request);
        return SuccessResponse.of(response);
    }

    @GetMapping("/me")
    @RequireAuth
    public SuccessResponse<UserProfileResponse> getMyProfile(
            @CurrentUserId Long currentUserId
    ) {
        UserProfileResponse profile = userService.getUserProfile(currentUserId);
        return SuccessResponse.of(profile);
    }

    @GetMapping("/me/souvenirs")
    @RequireAuth
    public SuccessResponse<MySouvenirListResponse> getMySouvenirs(
            @CurrentUserId Long currentUserId,
            @RequestParam int page,
            @RequestParam int size
    ) {
        MySouvenirListResponse souvenirs = userService.getMySouvenirs(currentUserId, page, size);
        return SuccessResponse.of(souvenirs);
    }

    @DeleteMapping("/me")
    @RequireAuth
    public SuccessResponse<Void> withdraw(@CurrentUserId Long currentUserId) {
        userService.withdraw(currentUserId);
        return SuccessResponse.of(null, "회원탈퇴가 완료되었습니다.");
    }

}
