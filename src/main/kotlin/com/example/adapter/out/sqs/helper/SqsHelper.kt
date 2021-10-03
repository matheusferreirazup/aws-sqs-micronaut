package com.example.adapter.out.sqs.helper

import com.example.application.config.objectToJson
import jakarta.inject.Singleton
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.util.stream.Collectors

@Singleton
class SqsHelper(private val sqsClient: SqsClient) {

    fun <T> sendMessage(queueUrl: String, body: T) {
        val messageRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(body.objectToJson()))
            .build()

        sqsClient.sendMessage(messageRequest)
    }

    fun sendMessage(queueUrl: String, message: String) {
        val messageRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(message)
            .build()

        sqsClient.sendMessage(messageRequest)
    }

    fun <T> receiveMessage(queueUrl: String, clazz: Class<T>): List<T> {
        val messageRequest = ReceiveMessageRequest.builder().queueUrl(queueUrl).waitTimeSeconds(3).build()
        val messageResponse: ReceiveMessageResponse = sqsClient.receiveMessage(messageRequest)

        return messageResponse.messages()
            .stream()
            .map { it.body().toObject(clazz) }
            .collect(Collectors.toList())
//            .flatMap { jacksonObjectMapper().readValue<List<T>>(it.body()) }
    }
}