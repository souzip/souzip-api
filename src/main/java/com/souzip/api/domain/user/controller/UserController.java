package com.souzip.api.domain.user.controller;

import com.souzip.api.domain.user.service.UserService;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.CurrentUserId;
import com.souzip.api.global.security.annotation.RequireAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;

    @DeleteMapping("/me")
    @RequireAuth
    public SuccessResponse<Void> withdraw(@CurrentUserId Long currentUserId) {
        userService.withdraw(currentUserId);
        return SuccessResponse.of(null, "회원탈퇴가 완료되었습니다.");
    }
}
