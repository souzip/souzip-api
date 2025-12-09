package com.souzip.api.global.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "ncp.object-storage")
public class ObjectStorageProperties {

    private final String endpoint;
    private final String region;
    private final String bucket;
    private final String accessKey;
    private final String secretKey;

}

