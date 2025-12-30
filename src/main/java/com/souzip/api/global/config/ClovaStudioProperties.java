package com.souzip.api.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ncp.clova-studio")
public class ClovaStudioProperties {
    private String apiKey;
    private String apiUrl;
}
