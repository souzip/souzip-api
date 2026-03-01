package com.souzip.domain.auth.client;

import com.souzip.domain.auth.dto.OAuthUserInfo;

public interface OAuthClient {
    OAuthUserInfo getUserInfo(String accessToken);
}
