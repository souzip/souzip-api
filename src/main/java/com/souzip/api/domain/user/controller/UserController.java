package com.souzip.api.domain.user.controller;

import com.souzip.api.domain.user.dto.NicknameCheckRequest;
import com.souzip.api.domain.user.dto.NicknameCheckResponse;
import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.dto.OnboardingResponse;
import com.souzip.api.domain.user.service.UserService;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.CurrentUserId;
import com.souzip.api.global.security.annotation.RequireAuth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @DeleteMapping("/me")
    @RequireAuth
    public SuccessResponse<Void> withdraw(@CurrentUserId Long currentUserId) {
        userService.withdraw(currentUserId);
        return SuccessResponse.of(null, "회원탈퇴가 완료되었습니다.");
    }
}
