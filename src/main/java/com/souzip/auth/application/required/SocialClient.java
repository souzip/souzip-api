package com.souzip.auth.application.required;

import com.souzip.auth.application.dto.SocialUserInfo;
import com.souzip.shared.domain.Provider;

public interface SocialClient {
    SocialUserInfo getUserInfo(String accessToken);

    Provider getProvider();
}