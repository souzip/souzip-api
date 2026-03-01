package com.souzip.global.security.jwt;

import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.repository.UserRepository;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            authenticateRequest(request);
        } catch (Exception e) {
            log.error("사용자 인증 정보를 SecurityContext에 설정할 수 없습니다.", e);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        if (isTokenAbsent(token)) {
            return;
        }

        User user = getUserFromToken(token);

        if (isUserAbsent(user)) {
            return;
        }

        setAuthentication(user);
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

    private User getUserFromToken(String token) {
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        return userRepository.findByUserId(userId).orElse(null);
    }

    private boolean isUserAbsent(User user) {
        return user == null;
    }

    private void setAuthentication(User user) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
