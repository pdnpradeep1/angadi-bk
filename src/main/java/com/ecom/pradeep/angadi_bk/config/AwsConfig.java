package com.ecom.pradeep.angadi_bk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1) // Change to your AWS region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Scope("prototype") // Ensures a new instance is created when needed
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.US_EAST_1) // Change this to your AWS region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
