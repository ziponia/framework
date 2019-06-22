package com.ziponia.aws.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsBeanFactory {

    @Value("${aws.accessKeyId}")
    private String AWS_ACCESS_KEY_ID;

    @Value("${aws.secretAccessKey}")
    private String AWS_SECRET_KEY;

    @Bean
    public StaticCredentialsProvider staticCredentialsProvider() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                AWS_ACCESS_KEY_ID, AWS_SECRET_KEY
        );
        return StaticCredentialsProvider.create(credentials);
    }

    @Bean
    public Ec2Client ec2Client() {
        return Ec2Client
                .builder()
                .credentialsProvider(staticCredentialsProvider())
                .build();
    }

    @Bean
    public Ec2AsyncClient ec2AsyncClient() {
        return Ec2AsyncClient.builder()
                .credentialsProvider(staticCredentialsProvider()).build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(staticCredentialsProvider())
                .build();
    }
}
