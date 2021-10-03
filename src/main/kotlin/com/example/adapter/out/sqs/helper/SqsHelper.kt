package com.example.adapter.out.sqs.helper

import com.example.application.config.jsonToObject
import com.example.application.config.objectToJson
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

@Singleton
class SqsHelper(private val sqsClient: SqsClient) {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SqsHelper::class.java)
    }

    fun <T> sendMessage(queueUrl: String, body: T) {
        val messageRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(body.objectToJson())
            .build()

        sqsClient.sendMessage(messageRequest)
    }

    fun <T> receiveMessage(queueUrl: String, clazz: Class<T>, waitTime: Int = 20): List<T> {
        LOGGER.info("Sending SQS request")

        val messageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .waitTimeSeconds(waitTime)
            .build()

        val messageResponse: ReceiveMessageResponse = sqsClient.receiveMessage(messageRequest)

        LOGGER.info("Number of messages found: ${messageResponse.messages().size}")
        LOGGER.info("Deserialize messages to class: ${clazz.name}")

        return messageResponse.messages()
            .map { it.body().jsonToObject(clazz) }
    }

    fun deleteMessages(queueUrl: String) {
        val purgeQueueRequest = PurgeQueueRequest.builder()
            .queueUrl(queueUrl)
            .build()

        sqsClient.purgeQueue(purgeQueueRequest)
    }
}