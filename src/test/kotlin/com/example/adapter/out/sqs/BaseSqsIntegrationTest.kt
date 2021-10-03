package com.example.adapter.out.sqs

import com.example.BaseEmbeddedServerTest
import com.example.adapter.out.sqs.domain.MessageVO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

abstract class BaseSqsIntegrationTest : BaseEmbeddedServerTest() {

    private val sqsClient: SqsClient = applicationContext.getBean(SqsClient::class.java)

    fun <T> sendMessage(message: T) {
        val queueUrl = testConfiguration()["aws.sqs.queueUrl"] as String
        val messageRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(message.toJson())
            .build()

        sqsClient.sendMessage(messageRequest)
    }

    fun getResourcesFromQueue(): List<MessageVO> {
        val queueUrl = testConfiguration()["aws.sqs.queueUrl"] as String
        val waitTime = testConfiguration()["aws.sqs.waitTime"] as Int
        val messageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .waitTimeSeconds(waitTime)
            .build()
        val messageResponse: ReceiveMessageResponse = sqsClient.receiveMessage(messageRequest)

        return messageResponse.messages()
            .flatMap { jacksonObjectMapper().readValue<List<MessageVO>>(it.body()) }
    }

    fun deleteAllMessages() {
        val queueUrl = testConfiguration()["aws.sqs.queueUrl"] as String
        val purgeQueueRequest = PurgeQueueRequest.builder()
            .queueUrl(queueUrl)
            .build()

        sqsClient.purgeQueue(purgeQueueRequest)
    }

    override fun testConfiguration(): Map<String, Any> {
        return mapOf(
            "aws.sqs.queueUrl" to "http://localhost:4566/000000000000/local-test-queue",
            "aws.sqs.waitTime" to 1
        )
    }
}