package com.souzip.application.email;

import com.souzip.application.email.provided.UserEmailFinder;
import com.souzip.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserEmailQueryService implements UserEmailFinder {

    private final UserRepository userRepository;

    @Override
    public List<String> findDistinctEmailsForActiveUsers() {
        return userRepository.findDistinctEmailsByActiveUsers().stream()
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .toList();
    }
}
