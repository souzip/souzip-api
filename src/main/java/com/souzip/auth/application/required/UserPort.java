package com.souzip.auth.application.required;

import com.souzip.auth.application.dto.UserInfo;
import com.souzip.shared.domain.Provider;

public interface UserPort {
    UserInfo findOrCreateUser(Provider provider, String providerId, String email);
}