package com.souzip.domain.user.adapter;

import com.souzip.auth.application.dto.UserInfo;
import com.souzip.auth.application.required.UserPort;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.repository.UserRepository;
import com.souzip.shared.domain.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPortImpl implements UserPort {

    private final UserRepository userRepository;

    @Override
    public UserInfo findOrCreateUser(Provider provider, String providerId, String email) {
        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userRepository.save(
                        User.of(provider, providerId, email)
                ));

        return new UserInfo(
                user.getId(),
                user.getUserId(),
                user.getNickname(),
                user.needsOnboarding()
        );
    }
}