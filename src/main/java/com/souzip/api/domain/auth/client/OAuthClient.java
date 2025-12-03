package com.souzip.api.domain.auth.client;

import com.souzip.api.domain.auth.dto.OAuthUserInfo;

public interface OAuthClient {

    OAuthUserInfo getUserInfo(String accessToken);
}
