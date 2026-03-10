package com.souzip.application.admin.dto;

public record AdminRefreshResult(
        String accessToken,
        String refreshToken
) {
}