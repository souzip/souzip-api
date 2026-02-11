package com.souzip.api.domain.admin.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Password {

    private final String encodedValue;

    public static Password encode(String rawPassword, AdminPasswordEncoder encoder) {
        return new Password(encoder.encode(rawPassword));
    }
}
