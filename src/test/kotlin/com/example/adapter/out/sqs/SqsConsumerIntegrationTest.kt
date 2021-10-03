package com.example.adapter.out.sqs

import com.example.adapter.out.sqs.domain.MessageVO
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.TimeUnit.SECONDS
import java.util.stream.Stream

internal class SqsConsumerIntegrationTest : BaseSqsIntegrationTest() {

    private val sqsConsumer = applicationContext.getBean(SqsConsumer::class.java)

    @BeforeEach
    fun init() {
        deleteAllMessages()
    }

    @Nested
    @DisplayName("Exception Tests")
    inner class SqsConsumerExceptions {

        @ParameterizedTest(name = "Throw MismatchedInputException when message is empty. Arguments: \"{arguments}\"")
        @EmptySource
        fun `When message is empty should throw MismatchedInputException`(messageBody: String) {
            assertThrows(MismatchedInputException::class.java) {
                sendMessage(messageBody)
                sqsConsumer.consumeQueue()
            }
        }

        @ParameterizedTest(name = "Throw MismatchedInputException when message cant deserialize (missing name attribute). Arguments: \"{arguments}\"")
        @ValueSource(
            strings = ["{\"description\":\"my-message-description\"}"]
        )
        fun `When message cant deserialize should throw MismatchedInputException`(messageBody: String) {
            assertThrows(MismatchedInputException::class.java) {
                sendMessage(messageBody)
                sqsConsumer.consumeQueue()
            }
        }
    }

    @ParameterizedTest(name = "Consume message in queue. Arguments: \"{argumentsWithNames}\"")
    @MethodSource("buildSingleMessage")
    fun `Should consume a message in SQS`(message: MessageVO, expected: List<MessageVO>) {
        sendMessage(message)
        val messages = sqsConsumer.consumeQueue()

        assertNotNull(messages)

        assertEquals(1, messages.size)
        assertEquals(message, messages[0])
    }

    @ParameterizedTest(name = "Receive messages and check if they are equal. Arguments: \"{argumentsWithNames}\\")
    @MethodSource("buildSingleMessage")
    fun `Should check same messages`(message: MessageVO, expected: List<MessageVO>) {
        sendMessage(message)

        await().atMost(3, SECONDS)
            .untilAsserted { assertEquals(expected, receiveMessage()) }
    }

    @ParameterizedTest(name = "Receive messages and check if they are NOT equal. Arguments: \"{argumentsWithNames}\\")
    @MethodSource("notEqualArguments")
    fun `Should check different messages`(message: MessageVO, expected: List<MessageVO>) {
        sendMessage(message)

        await().atMost(3, SECONDS)
            .untilAsserted { assertNotEquals(expected, receiveMessage()) }
    }

    private companion object {
        @JvmStatic
        fun buildSingleMessage(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    MessageVO(
                        name = "my-message-name",
                        description = "my-message-description"
                    ),
                    arrayListOf(
                        MessageVO(
                            name = "my-message-name",
                            description = "my-message-description"
                        )
                    )
                )
            )

        @JvmStatic
        fun notEqualArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    MessageVO(
                        name = "my-message-name",
                        description = "my-message-description"
                    ),
                    arrayListOf(
                        MessageVO(
                            name = "not-same-message-name",
                            description = "not-same-message-description"
                        )
                    )
                )
            )
    }

}