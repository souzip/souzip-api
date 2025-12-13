package com.souzip.api.domain.auth.dto;

public interface OAuthUserInfo {
    String getProviderId();
    String getName();
    String getEmail();
    default String getProfileImageUrl() {
        return null;
    }
}
