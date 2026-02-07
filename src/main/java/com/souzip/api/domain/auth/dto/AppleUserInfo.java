package com.souzip.api.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppleUserInfo implements OAuthUserInfo {

    private String sub;
    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    @JsonProperty("transfer_sub")
    private String transferSub;

    @Override
    public String getProviderId() {
        return sub;
    }

    @Override
    public String getName() {
        if (isValidEmail(email)) {
            return email.split("@")[0];
        }
        return "apple.user";
    }

    @Override
    public String getEmail() {
        return email;
    }

    public String getTransferSub() {
        return transferSub;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
