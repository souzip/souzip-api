package com.souzip.api.domain.user.service;

import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import com.souzip.api.adapter.config.ObjectStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class ProfileImageService {

    private final ObjectStorageProperties storageProperties;

    private static final String PROFILE_FOLDER = "profile";
    private static final String IMAGE_EXTENSION = ".png";
    private static final String DEFAULT_COLOR = "red";
    private static final String URL_FORMAT = "%s/%s/%s/%s%s";

    private static final Set<String> AVAILABLE_COLORS = Set.of(
        "red", "blue", "yellow", "purple"
    );

    public String resolveProfileImageUrl(String input) {
        if (isNullOrBlank(input)) {
            return buildDefaultColorUrl();
        }

        if (isFullUrl(input)) {
            return input;
        }

        return buildColorUrlOrThrow(input);
    }

    public Set<String> getAvailableColors() {
        return AVAILABLE_COLORS;
    }

    private boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isFullUrl(String value) {
        return value.startsWith("https://");
    }

    private String buildDefaultColorUrl() {
        return buildColorUrl(DEFAULT_COLOR);
    }

    private String buildColorUrlOrThrow(String input) {
        String normalizedColor = normalizeColor(input);

        if (isValidColor(normalizedColor)) {
            return buildColorUrl(normalizedColor);
        }

        throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE_COLOR);
    }

    private String normalizeColor(String color) {
        return color.toLowerCase().trim();
    }

    private boolean isValidColor(String color) {
        return AVAILABLE_COLORS.contains(color);
    }

    private String buildColorUrl(String color) {
        return String.format(
            URL_FORMAT,
            storageProperties.getEndpoint(),
            storageProperties.getBucket(),
            PROFILE_FOLDER,
            color,
            IMAGE_EXTENSION
        );
    }
}
