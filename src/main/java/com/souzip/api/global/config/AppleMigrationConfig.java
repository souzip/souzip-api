package com.souzip.api.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties(prefix = "oauth.apple")
public class AppleMigrationConfig {

    private final String teamId;
    private final String oldTeamId;
    private final String clientId;
    private final String keyId;
    private final String privateKey;

    @ConstructorBinding
    public AppleMigrationConfig(
        String teamId,
        String oldTeamId,
        String clientId,
        String keyId,
        String privateKey
    ) {
        this.teamId = teamId;
        this.oldTeamId = oldTeamId;
        this.clientId = clientId;
        this.keyId = keyId;
        this.privateKey = privateKey;
    }
}
