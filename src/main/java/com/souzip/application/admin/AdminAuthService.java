package com.souzip.application.admin;

import com.souzip.application.admin.dto.AdminLoginResult;
import com.souzip.application.admin.dto.AdminRefreshResult;
import com.souzip.application.admin.required.AdminRefreshTokenRepository;
import com.souzip.application.admin.required.AdminRepository;
import com.souzip.application.admin.required.TokenProvider;
import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminRefreshToken;
import com.souzip.domain.admin.PasswordEncoder;
import com.souzip.domain.admin.exception.AdminExpiredRefreshTokenException;
import com.souzip.domain.admin.exception.AdminInvalidRefreshTokenException;
import com.souzip.domain.admin.exception.AdminLoginFailedException;
import com.souzip.domain.admin.exception.AdminNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminAuthService {

    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 30;
    private static final int REFRESH_TOKEN_RENEWAL_THRESHOLD_DAYS = 10;

    private final AdminRepository adminRepository;
    private final AdminRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public AdminLoginResult login(String username, String password) {
        Admin admin = findAndValidateAdmin(username, password);

        admin.login();
        adminRepository.save(admin);

        String accessToken = tokenProvider.generateAccessToken(admin.getId().toString());
        String refreshToken = tokenProvider.generateRefreshToken(admin.getId().toString());

        saveOrUpdateRefreshToken(admin.getId(), refreshToken);

        return new AdminLoginResult(admin, accessToken, refreshToken);
    }

    @Transactional
    public AdminRefreshResult refresh(String refreshTokenValue) {
        AdminRefreshToken refreshToken = findValidRefreshToken(refreshTokenValue);
        Admin admin = adminRepository.findById(refreshToken.getAdminId())
                .orElseThrow(AdminNotFoundException::new);

        String newAccessToken = tokenProvider.generateAccessToken(admin.getId().toString());

        if (isExpiringSoon(refreshToken)) {
            return renewRefreshToken(refreshToken, newAccessToken);
        }

        return new AdminRefreshResult(newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(UUID adminId) {
        refreshTokenRepository.findByAdminId(adminId)
                .ifPresent(refreshTokenRepository::delete);
    }

    private Admin findAndValidateAdmin(String username, String password) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(AdminNotFoundException::new);

        if (!admin.matchesPassword(password, passwordEncoder)) {
            throw new AdminLoginFailedException();
        }

        return admin;
    }

    private AdminRefreshToken findValidRefreshToken(String tokenValue) {
        AdminRefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(AdminInvalidRefreshTokenException::new);

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AdminExpiredRefreshTokenException();
        }

        return refreshToken;
    }

    private AdminRefreshResult renewRefreshToken(AdminRefreshToken refreshToken, String newAccessToken) {
        String newRefreshToken = tokenProvider.generateRefreshToken(
                refreshToken.getAdminId().toString()
        );

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);
        refreshToken.updateToken(newRefreshToken, expiresAt);

        refreshTokenRepository.save(refreshToken);

        return new AdminRefreshResult(newAccessToken, newRefreshToken);
    }

    private void saveOrUpdateRefreshToken(UUID adminId, String tokenValue) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);
        refreshTokenRepository.findByAdminId(adminId)
                .ifPresentOrElse(
                        token -> {
                            token.updateToken(tokenValue, expiresAt);
                            refreshTokenRepository.save(token);
                        },
                        () -> refreshTokenRepository.save(
                                AdminRefreshToken.create(adminId, tokenValue, expiresAt)
                        )
                );
    }

    private boolean isExpiringSoon(AdminRefreshToken refreshToken) {
        return refreshToken.getExpiresAt()
                .isBefore(LocalDateTime.now().plusDays(REFRESH_TOKEN_RENEWAL_THRESHOLD_DAYS));
    }
}