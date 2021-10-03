package com.example.adapter.out.sqs

import com.example.adapter.out.sqs.domain.MessageVO
import com.example.application.config.JacksonExtension
import com.fasterxml.jackson.core.type.TypeReference
import io.micronaut.context.annotation.Value
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse

@Singleton
class SqsConsumer(
    @Value("\${aws.sqs.queueUrl}")
    private val queueUrl: String,
    @Value("\${aws.sqs.waitTime}")
    private val waitTime: Int,
    private val sqsClient: SqsClient,
) {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SqsConsumer::class.java)
    }

    @Scheduled(initialDelay = "5s", fixedDelay = "\${aws.sqs.timePolling}")
    fun consumeResourcesQueue() {
        LOGGER.info("Starting SQS consumer")

        val resources: List<MessageVO> = getResourcesFromQueue()

    }

    private fun getResourcesFromQueue(): List<MessageVO> {
        LOGGER.info("Sending SQS request")

        val messageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .waitTimeSeconds(waitTime)
            .build()
        val messageResponse: ReceiveMessageResponse = sqsClient.receiveMessage(messageRequest)

        LOGGER.info("Number of messages found: ${messageResponse.messages().size}")

        return messageResponse.messages()
            .flatMap {
                JacksonExtension.jacksonObjectMapper.readValue(
                    Regex.escapeReplacement(it.body()),
                    object : TypeReference<List<ResourceVO>>() {}
                )
            }
    }
}