package com.souzip.api.domain.user.controller;

import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.dto.OnboardingResponse;
import com.souzip.api.domain.user.dto.ProfileColorsResponse;
import com.souzip.api.domain.user.service.UserService;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.CurrentUserId;
import com.souzip.api.global.security.annotation.RequireAuth;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;

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
