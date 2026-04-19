package com.souzip.auth.application.provided;

import com.souzip.auth.application.dto.LoginInfo;
import com.souzip.auth.application.dto.RefreshInfo;
import com.souzip.domain.shared.Provider;

public interface Auth {
    LoginInfo login(Provider provider, String accessToken);

    RefreshInfo refresh(String refreshToken);

    void logout(Long userId);
}