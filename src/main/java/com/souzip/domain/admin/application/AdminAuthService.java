package com.souzip.domain.admin.application;

import com.souzip.domain.admin.application.command.AdminLoginCommand;
import com.souzip.domain.admin.exception.AdminExpiredRefreshTokenException;
import com.souzip.domain.admin.exception.AdminInvalidRefreshTokenException;
import com.souzip.domain.admin.exception.AdminLoginFailedException;
import com.souzip.domain.admin.exception.AdminNotFoundException;
import com.souzip.domain.admin.model.Admin;
import com.souzip.domain.admin.model.AdminPasswordEncoder;
import com.souzip.domain.admin.model.AdminRefreshToken;
import com.souzip.domain.admin.model.Username;
import com.souzip.domain.admin.repository.AdminRefreshTokenRepository;
import com.souzip.domain.admin.repository.AdminRepository;
import com.souzip.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AdminAuthService {

    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 30;
    private static final int REFRESH_TOKEN_RENEWAL_THRESHOLD_DAYS = 10;

    private final AdminRepository adminRepository;
    private final AdminRefreshTokenRepository refreshTokenRepository;
    private final AdminPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AdminLoginResult login(AdminLoginCommand command) {
        Admin admin = adminRepository.findByUsername(new Username(command.username()))
            .orElseThrow(AdminNotFoundException::new);

        validatePassword(admin, command.password());

        admin.recordLoginSuccess();
        Admin savedAdmin = adminRepository.save(admin);

        String accessToken = jwtTokenProvider.generateToken(savedAdmin.getId().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedAdmin.getId().toString());

        saveRefreshToken(savedAdmin, refreshToken);

        return new AdminLoginResult(savedAdmin, accessToken, refreshToken);
    }

    @Transactional
    public RefreshResult refresh(String refreshTokenValue) {
        AdminRefreshToken refreshToken = findRefreshToken(refreshTokenValue);
        validateRefreshToken(refreshToken);

        Admin admin = adminRepository.findById(refreshToken.getAdminId())
            .orElseThrow(AdminNotFoundException::new);

        String newAccessToken = jwtTokenProvider.generateToken(admin.getId().toString());

        if (isRefreshTokenExpiringSoon(refreshToken)) {
            return renewRefreshToken(refreshToken, admin, newAccessToken);
        }

        return new RefreshResult(newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(UUID adminId) {
        refreshTokenRepository.findByAdminId(adminId)
            .ifPresent(refreshTokenRepository::delete);
    }

    private void validatePassword(Admin admin, String password) {
        if (!admin.matchesPassword(password, passwordEncoder)) {
            throw new AdminLoginFailedException();
        }
    }

    private void saveRefreshToken(Admin admin, String tokenValue) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);

        refreshTokenRepository.findByAdminId(admin.getId())
            .ifPresentOrElse(
                token -> updateExistingToken(token, tokenValue, expiresAt),
                () -> createNewToken(admin.getId(), tokenValue, expiresAt)
            );
    }

    private void updateExistingToken(AdminRefreshToken token, String tokenValue, LocalDateTime expiresAt) {
        token.updateToken(tokenValue, expiresAt);
        refreshTokenRepository.save(token);
    }

    private void createNewToken(UUID adminId, String tokenValue, LocalDateTime expiresAt) {
        AdminRefreshToken newToken = AdminRefreshToken.create(adminId, tokenValue, expiresAt);
        refreshTokenRepository.save(newToken);
    }

    private AdminRefreshToken findRefreshToken(String tokenValue) {
        return refreshTokenRepository.findByToken(tokenValue)
            .orElseThrow(AdminInvalidRefreshTokenException::new);
    }

    private void validateRefreshToken(AdminRefreshToken refreshToken) {
        if (refreshToken.isExpired()) {
            deleteExpiredToken(refreshToken);
            throw new AdminExpiredRefreshTokenException();
        }
    }

    private void deleteExpiredToken(AdminRefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
        log.info("만료된 Admin Refresh Token 삭제: token={}", refreshToken.getId());
    }

    private boolean isRefreshTokenExpiringSoon(AdminRefreshToken refreshToken) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(REFRESH_TOKEN_RENEWAL_THRESHOLD_DAYS);
        return refreshToken.getExpiresAt().isBefore(threshold);
    }

    private RefreshResult renewRefreshToken(AdminRefreshToken refreshToken, Admin admin, String newAccessToken) {
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(admin.getId().toString());
        updateExistingToken(refreshToken, newRefreshToken, LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS));

        log.info("Admin Refresh Token 갱신: adminId={}", admin.getId());

        return new RefreshResult(newAccessToken, newRefreshToken);
    }

    public record AdminLoginResult(
        Admin admin,
        String accessToken,
        String refreshToken
    ) {}

    public record RefreshResult(
        String accessToken,
        String refreshToken
    ) {}
}
