package com.souzip.domain.user.service;

import com.souzip.shared.exception.BusinessException;
import com.souzip.shared.exception.ErrorCode;
import com.souzip.adapter.config.ObjectStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileImageServiceTest {

    private ProfileImageService profileImageService;
    private ObjectStorageProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ObjectStorageProperties(
                "https://kr.object.ncloudstorage.com",
                "kr-standard",
                "souzip-dev-images",
                "test-access-key",
                "test-secret-key"
        );
        profileImageService = new ProfileImageService(properties);
    }

    @Test
    @DisplayName("유효한 색상으로 프로필 이미지 URL을 생성한다.")
    void resolveProfileImageUrl_withValidColor() {
        // when
        String url = profileImageService.resolveProfileImageUrl("red");

        // then
        assertThat(url).isEqualTo("https://kr.object.ncloudstorage.com/souzip-dev-images/profile/red.png");
    }

    @ParameterizedTest
    @ValueSource(strings = {"red", "blue", "yellow", "purple"})
    @DisplayName("모든 유효한 색상에 대해 올바른 URL을 생성한다.")
    void resolveProfileImageUrl_allValidColors(String color) {
        // when
        String url = profileImageService.resolveProfileImageUrl(color);

        // then
        assertThat(url)
                .startsWith("https://kr.object.ncloudstorage.com/souzip-dev-images/profile/")
                .endsWith(color + ".png");
    }

    @ParameterizedTest
    @ValueSource(strings = {"RED", "Blue", "YELLOW", "PuRpLe"})
    @DisplayName("대소문자 구분 없이 색상을 처리한다.")
    void resolveProfileImageUrl_caseInsensitive(String color) {
        // when
        String url = profileImageService.resolveProfileImageUrl(color);

        // then
        assertThat(url).contains("/" + color.toLowerCase() + ".png");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("null이나 빈 문자열이 입력되면 기본 색상(red)을 반환한다.")
    void resolveProfileImageUrl_withNullOrEmpty_returnsDefault(String input) {
        // when
        String url = profileImageService.resolveProfileImageUrl(input);

        // then
        assertThat(url).isEqualTo("https://kr.object.ncloudstorage.com/souzip-dev-images/profile/red.png");
    }

    @ParameterizedTest
    @ValueSource(strings = {"green", "orange", "pink", "black", "invalid"})
    @DisplayName("유효하지 않은 색상이 입력되면 예외가 발생한다.")
    void resolveProfileImageUrl_withInvalidColor_throwsException(String invalidColor) {
        // when & then
        assertThatThrownBy(() -> profileImageService.resolveProfileImageUrl(invalidColor))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROFILE_IMAGE_COLOR)
                .hasMessageContaining("유효하지 않은 프로필 이미지 색상입니다.");
    }

    @Test
    @DisplayName("생성된 URL은 올바른 형식을 따른다.")
    void resolveProfileImageUrl_urlFormat() {
        // when
        String url = profileImageService.resolveProfileImageUrl("blue");

        // then
        assertThat(url)
                .matches("https://.*\\.ncloudstorage\\.com/.*/profile/[a-z]+\\.png");
    }

    @Test
    @DisplayName("사용 가능한 색상 목록을 반환한다.")
    void getAvailableColors() {
        // when
        var colors = profileImageService.getAvailableColors();

        // then
        assertThat(colors)
                .containsExactlyInAnyOrder("red", "blue", "yellow", "purple")
                .hasSize(4);
    }
}
