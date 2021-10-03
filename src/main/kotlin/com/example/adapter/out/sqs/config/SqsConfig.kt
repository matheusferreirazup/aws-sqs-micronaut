package com.example.adapter.out.sqs.config

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Factory
class SqsConfig(
    @Value("\${aws.sqs.endpoint}")
    val endpoint: String,
    @Value("\${aws.region}")
    val region: String,
    @Value("\${aws.accessKeyId}")
    val accessKey: String,
    @Value("\${aws.secretKey}")
    val secretKey: String
) {

    @Bean
    @Primary
    @Requires(notEnv = ["k8s"])
    fun amazonSqsAsyncLocal(): SqsClient {
        return SqsClient.builder()
            .credentialsProvider(credentialsProvider())
            .endpointOverride(endpointConfiguration())
            .build()
    }

    @Bean
    @Primary
    @Requires(env = ["k8s"])
    fun amazonSqsAsyncCloud(): SqsClient {
        return SqsClient.builder()
            .build()
    }

    private fun credentialsProvider() =
        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))

    private fun endpointConfiguration() = URI.create(endpoint)
}