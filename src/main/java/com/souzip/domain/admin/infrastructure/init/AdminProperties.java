package com.souzip.domain.admin.infrastructure.init;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "admin.initial")
public class
AdminProperties {
    private final String username;
    private final String password;

    public AdminProperties(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
