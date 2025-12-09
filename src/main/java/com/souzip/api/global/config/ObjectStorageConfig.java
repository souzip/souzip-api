package com.souzip.api.global.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.souzip.api.global.storage.ObjectStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j  // ⬅️ 추가
@Configuration
@RequiredArgsConstructor
public class ObjectStorageConfig {

    private final ObjectStorageProperties properties;

    @Bean
    public AmazonS3 s3Client() {
        // ⭐ 로그 추가
        log.info("=== ObjectStorage Config ===");
        log.info("Endpoint: {}", properties.getEndpoint());
        log.info("Region: {}", properties.getRegion());
        log.info("Bucket: {}", properties.getBucket());
        log.info("Access Key: {}", properties.getAccessKey() != null ? "설정됨 ✅" : "NULL ❌");
        log.info("Secret Key: {}", properties.getSecretKey() != null ? "설정됨 ✅" : "NULL ❌");

        BasicAWSCredentials credentials = new BasicAWSCredentials(
            properties.getAccessKey(),
            properties.getSecretKey()
        );

        return AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    properties.getEndpoint(),
                    properties.getRegion()
                )
            )
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();
    }
}
