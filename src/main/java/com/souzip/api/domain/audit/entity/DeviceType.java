package com.souzip.api.domain.audit.entity;

import java.util.Arrays;

public enum DeviceType {
    IOS,
    ANDROID,
    UNKNOWN;

    private static final String[] IOS_KEYWORDS = {"IOS", "IPHONE", "IPAD"};
    private static final String[] ANDROID_KEYWORDS = {"ANDROID"};

    public static DeviceType fromUserAgent(String userAgent) {
        if (isNullOrEmpty(userAgent)) {
            return UNKNOWN;
        }

        String normalizedUserAgent = normalize(userAgent);

        if (isIosDevice(normalizedUserAgent)) {
            return IOS;
        }

        if (isAndroidDevice(normalizedUserAgent)) {
            return ANDROID;
        }

        return UNKNOWN;
    }

    private static boolean isIosDevice(String normalizedUserAgent) {
        return containsAnyKeyword(normalizedUserAgent, IOS_KEYWORDS);
    }

    private static boolean isAndroidDevice(String normalizedUserAgent) {
        return containsAnyKeyword(normalizedUserAgent, ANDROID_KEYWORDS);
    }

    private static boolean containsAnyKeyword(String userAgent, String[] keywords) {
        return Arrays.stream(keywords)
            .anyMatch(userAgent::contains);
    }

    private static String normalize(String userAgent) {
        return userAgent.toUpperCase();
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
