package com.souzip.api.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfo {

    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    public static class KakaoAccount {

        private Profile profile;

        @Getter
        @NoArgsConstructor
        public static class Profile {
            private String nickname;
        }
    }

    public String getProviderId() {
        return String.valueOf(id);
    }

    public String getNickname() {
        return kakaoAccount.profile.nickname;
    }
}
