package com.souzip.api.domain.auth.controller;

import com.souzip.api.domain.auth.dto.LoginRequest;
import com.souzip.api.domain.auth.dto.LoginResponse;
import com.souzip.api.domain.auth.service.AuthService;
import com.souzip.api.domain.user.entity.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/kakao")
    public ResponseEntity<LoginResponse> loginWithKakao(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(Provider.KAKAO, request.accessToken());
        return ResponseEntity.ok(response);
    }
}
