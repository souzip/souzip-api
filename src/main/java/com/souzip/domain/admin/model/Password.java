package com.souzip.domain.admin.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Password {

    private final String encodedValue;

    public static Password encode(String rawPassword, AdminPasswordEncoder encoder) {
        return new Password(encoder.encode(rawPassword));
    }

    public static Password of(String encodedValue) {
        return new Password(encodedValue);
    }

    public boolean matches(String rawPassword, AdminPasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.encodedValue);
    }

    public String getEncodedValue() {
        return encodedValue;
    }
}
