package com.souzip.auth.application.exception;

import com.souzip.shared.exception.BusinessException;

public class AuthException extends BusinessException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }

    public static AuthException invalidRefreshToken() {
        return new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    public static AuthException expiredRefreshToken() {
        return new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    public static AuthException unsupportedProvider() {
        return new AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER);
    }

    public static AuthException invalidJwtStructure() {
        return new AuthException(AuthErrorCode.INVALID_JWT_STRUCTURE);
    }

    public static AuthException tokenDecodeFailed() {
        return new AuthException(AuthErrorCode.TOKEN_DECODE_FAILED);
    }

    public static AuthException tokenParseFailed() {
        return new AuthException(AuthErrorCode.TOKEN_PARSE_FAILED);
    }

    public static AuthException kakaoApiError() {
        return new AuthException(AuthErrorCode.KAKAO_API_ERROR);
    }

    public static AuthException googleApiError() {
        return new AuthException(AuthErrorCode.GOOGLE_API_ERROR);
    }

    public static AuthException appleApiError() {
        return new AuthException(AuthErrorCode.APPLE_API_ERROR);
    }
}