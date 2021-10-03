package com.example.adapter.out.sqs

import com.example.adapter.out.sqs.domain.MessageVO
import com.example.adapter.out.sqs.helper.SqsHelper
import io.micronaut.context.annotation.Value
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class SqsConsumer(
    @Value("\${aws.sqs.queueUrl}")
    private val queueUrl: String,
    @Value("\${aws.sqs.waitTime}")
    private val waitTime: Int,
    private val sqsHelper: SqsHelper
) {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SqsConsumer::class.java)
    }

    @Scheduled(initialDelay = "\${aws.sqs.initialDelay}", fixedDelay = "\${aws.sqs.timePolling}")
    fun consumeQueue(): List<MessageVO> {
        LOGGER.info("Starting SQS consumer")

        val messages: List<MessageVO> = sqsHelper.receiveMessage(queueUrl, MessageVO::class.java, waitTime)

        for (message in messages) {
            LOGGER.info("Message: $message")
        }

        return messages
    }
}