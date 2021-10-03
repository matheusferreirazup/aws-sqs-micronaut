package com.example.adapter.out.sqs

import com.example.adapter.out.sqs.domain.MessageVO
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.*
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
    private val repository = applicationContext.getBean(ResourceRepository::class.java)

    @BeforeEach
    fun clean() {
        repository.deleteAll()
//        deleteAllMessages()
    }

    @Nested
    @DisplayName("Exception Tests")
    inner class SqsConsumerExceptions {

        @ParameterizedTest(name = "Throw MismatchedInputException when message is empty. Arguments: \"{arguments}\"")
        @EmptySource
        fun `When message is empty should throw MismatchedInputException`(messageBody: String) {
            assertThrows(MismatchedInputException::class.java) {
                sendMessage(JacksonExtension.jacksonObjectMapper.readValue<List<MessageVO>>(messageBody))
                sqsConsumer.consumeResourcesQueue()
            }

            assertEquals(0, repository.count())
        }

        @ParameterizedTest(name = "Throw MismatchedInputException when message cant deserialize (missing name attribute). Arguments: \"{arguments}\"")
        @ValueSource(
            strings = ["[{\"ref\":1,\"type\":\"Symbol\",\"description\":\"Symbol FW3 for Itau Bank\", " +
                    "\"parent\": {\"account::dev::e5d5cc5c-b0b0-4240-ba05-e95cfead0d87\"}}]"]
        )
        fun `When message cant deserialize should throw MismatchedInputException`(messageBody: String) {
            assertThrows(MismatchedInputException::class.java) {
                sendMessage(JacksonExtension.jacksonObjectMapper.readValue<List<MessageVO>>(messageBody))
                sqsConsumer.consumeResourcesQueue()
            }

            assertEquals(0, repository.count())
        }
    }

    @ParameterizedTest(name = "Consume message in queue. Arguments: \"{argumentsWithNames}\"")
    @MethodSource("buildSingleResourceVO")
    fun `Should consume a message in SQS`(message: List<MessageVO>) {
        sendMessage(message)
        sqsConsumer.consumeResourcesQueue()
        assertNotEquals(0, repository.count())
    }

    @ParameterizedTest(name = "Receive messages and check if they are equal. Arguments: \"{argumentsWithNames}\\")
    @MethodSource("equalArguments")
    fun `Should check same messages`(message: List<MessageVO>, expected: List<MessageVO>) {
        sendMessage(message)

        await().atMost(3, SECONDS)
            .untilAsserted { assertEquals(expected, getResourcesFromQueue()) }
    }

    @ParameterizedTest(name = "Receive messages and check if they are NOT equal. Arguments: \"{argumentsWithNames}\\")
    @MethodSource("notEqualArguments")
    fun `Should check different messages`(message: List<MessageVO>, expected: List<MessageVO>) {
        sendMessage(message)

        await().atMost(3, SECONDS)
            .untilAsserted { assertNotEquals(expected, getResourcesFromQueue()) }
    }

    private companion object {
        const val data = "{\"arn\":\"my-arn\",\"containerName\":\"cartao-branco-application\",\"containerQuantity\":2}"

        @JvmStatic
        fun buildSingleResourceVO() =
            Stream.of(
                Arguments.of(
                    arrayListOf(
                        MessageVO(
                            ref = 1,
                            type = "GROUP",
                            name = "FW3",
                            description = "Group FW3 for Itau Bank"
                        )
                    )
                )
            )

        @JvmStatic
        fun equalArguments() =
            Stream.of(
                Arguments.of(
                    arrayListOf(
                        MessageVO(
                            ref = 1,
                            type = "GROUP",
                            name = "FW3",
                            description = "Group FW3 for Itau Bank",
                            parent = "account::dev::e5d5cc5c-b0b0-4240-ba05-e95cfead0d87",
                        ),
                        MessageVO(
                            ref = 2,
                            type = "PRODUCT",
                            name = "CartaoBranco",
                            description = "Application of Cartao Branco",
                            data = data.toJsonNode(),
                            parentRef = 1
                        )
                    ),
                    arrayListOf(
                        MessageVO(
                            ref = 1,
                            type = "GROUP",
                            name = "FW3",
                            description = "Group FW3 for Itau Bank",
                            parent = "account::dev::e5d5cc5c-b0b0-4240-ba05-e95cfead0d87",
                        ),
                        MessageVO(
                            ref = 2,
                            type = "PRODUCT",
                            name = "CartaoBranco",
                            description = "Application of Cartao Branco",
                            data = data.toJsonNode(),
                            parentRef = 1
                        )
                    )
                )
            )

        @JvmStatic
        fun notEqualArguments() =
            Stream.of(
                Arguments.of(
                    arrayListOf(
                        MessageVO(
                            ref = 1,
                            type = "GROUP",
                            name = "FW3",
                            description = "Group FW3 for Itau Bank",
                            parent = "account::dev::e5d5cc5c-b0b0-4240-ba05-e95cfead0d87"
                        ),
                        MessageVO(
                            ref = 2,
                            type = "PRODUCT",
                            name = "CartaoBranco",
                            description = "Application of Cartao Branco",
                            data = data.toJsonNode(),
                            parentRef = 1
                        )
                    ),
                    arrayListOf(
                        MessageVO(
                            ref = 1,
                            type = "GROUP",
                            name = "FW3",
                            description = "Group FW3 for Itau Bank",
                            parent = "account::dev::e5d5cc5c-b0b0-4240-ba05-e95cfead0d87"
                        ),
                        MessageVO(
                            ref = 2,
                            type = "PRODUCT",
                            name = "CartaoBranco",
                            description = "Application of Cartao Branco",
                            data = data.toJsonNode(),
                            parent = "account::dev::e5d5cc5c-b0b0-4240-ba05-e95cfead0d87"
                        ),
                        MessageVO(
                            ref = 3,
                            type = "PRODUCT",
                            name = "New Application",
                            description = "Application of New Application",
                            data = data.toJsonNode(),
                            parent = "account::dev::e5d5cc5c-b0b0-4240-ba05-e95cfead0d87"
                        )
                    )
                )
            )
    }

}