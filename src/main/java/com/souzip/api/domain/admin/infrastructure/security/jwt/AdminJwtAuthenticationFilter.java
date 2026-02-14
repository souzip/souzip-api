package com.souzip.api.domain.admin.infrastructure.security.jwt;

import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.repository.AdminRepository;
import com.souzip.api.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_PATH_PREFIX = "/api/admin";

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminRepository adminRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (isAdminPath(request)) {
            try {
                authenticateAdmin(request);
            } catch (Exception e) {
                log.error("Admin 인증 정보를 SecurityContext에 설정할 수 없습니다.", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAdminPath(HttpServletRequest request) {
        return request.getRequestURI().startsWith(ADMIN_PATH_PREFIX);
    }

    private void authenticateAdmin(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        if (isTokenAbsent(token)) {
            return;
        }

        Admin admin = getAdminFromToken(token);

        if (isAdminAbsent(admin)) {
            return;
        }

        setAuthentication(admin);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (hasBearerToken(bearerToken)) {
            return extractToken(bearerToken);
        }

        return null;
    }

    private boolean hasBearerToken(String bearerToken) {
        return StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX);
    }

    private String extractToken(String bearerToken) {
        return bearerToken.substring(BEARER_PREFIX.length());
    }

    private boolean isTokenAbsent(String token) {
        return isTokenNull(token) || isTokenInvalid(token);
    }

    private boolean isTokenNull(String token) {
        return token == null;
    }

    private boolean isTokenInvalid(String token) {
        return !jwtTokenProvider.validateToken(token);
    }

    private Admin getAdminFromToken(String token) {
        String adminId = jwtTokenProvider.getUserIdFromToken(token);
        return adminRepository.findById(UUID.fromString(adminId)).orElse(null);
    }

    private boolean isAdminAbsent(Admin admin) {
        return admin == null;
    }

    private void setAuthentication(Admin admin) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(admin, null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
