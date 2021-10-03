package com.example.adapter.out.sqs

import com.example.BaseEmbeddedServerTest
import com.example.adapter.out.sqs.domain.MessageVO
import com.example.adapter.out.sqs.helper.SqsHelper

abstract class BaseSqsIntegrationTest : BaseEmbeddedServerTest() {

    private val sqsHelper: SqsHelper = applicationContext.getBean(SqsHelper::class.java)

    fun <T> sendMessage(message: T) {
        val queueUrl = testConfiguration()["aws.sqs.queueUrl"] as String

        sqsHelper.sendMessage(queueUrl, message)
    }

    fun receiveMessage(): List<MessageVO> {
        val queueUrl = testConfiguration()["aws.sqs.queueUrl"] as String
        val waitTime = testConfiguration()["aws.sqs.waitTime"] as Int

        return sqsHelper.receiveMessage(queueUrl, MessageVO::class.java, waitTime)
    }

    fun deleteAllMessages() {
        val queueUrl = testConfiguration()["aws.sqs.queueUrl"] as String

        sqsHelper.deleteMessages(queueUrl)
    }

    override fun testConfiguration(): Map<String, Any> {
        return mapOf(
            "aws.sqs.queueUrl" to "http://localhost:4566/000000000000/local-test-queue",
            "aws.sqs.waitTime" to 1
        )
    }
}