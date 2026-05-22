package com.souzip.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties(prefix = "oauth.apple")
public class AppleMigrationConfig {

    private final String clientId;
    private final String teamId;
    private final String keyId;
    private final String privateKey;
    private final String oldTeamId;
    private final String oldKeyId;
    private final String oldPrivateKey;

    @ConstructorBinding
    public AppleMigrationConfig(
        String clientId,
        String teamId,
        String keyId,
        String privateKey,
        String oldTeamId,
        String oldKeyId,
        String oldPrivateKey
    ) {
        this.clientId = clientId;
        this.teamId = teamId;
        this.keyId = keyId;
        this.privateKey = privateKey;
        this.oldTeamId = oldTeamId;
        this.oldKeyId = oldKeyId;
        this.oldPrivateKey = oldPrivateKey;
    }
}
