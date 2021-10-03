package com.example.application.config

import com.example.application.config.JacksonExtension.jacksonObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.InputStream

object JacksonExtension {

    val jacksonObjectMapper: ObjectMapper by lazy {
        val kotlinModule = KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, true)
            .configure(KotlinFeature.NullToEmptyMap, true)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()

        ObjectMapper().registerModule(kotlinModule)
            .also { it.registerModule(SimpleModule()) }
            .also { it.registerModule(JavaTimeModule()) }
            .also { it.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) }
            .also { it.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true) }
            .also { it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
            .also { it.setSerializationInclusion(JsonInclude.Include.NON_NULL) }
            .also { it.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE) }
            .also { it.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true) }
    }
}

fun <T> JsonNode.toObject(t: Class<T>): T? =
    JacksonExtension.jacksonObjectMapper.convertValue(this, t)

fun JsonNode?.toMap(): Map<Any, Any>? = this?.let {
    val typeRef = object : TypeReference<LinkedHashMap<Any, Any>>() {}
    return JacksonExtension.jacksonObjectMapper.convertValue(this, typeRef)
}

fun <T> String.jsonToObject(t: Class<T>): T =
    jacksonObjectMapper.readValue(this, t)

fun <T> String.jsonToObject(typeReference: TypeReference<T>): T =
    jacksonObjectMapper.readValue(this, typeReference)

fun String.jsonToMap(): HashMap<Any, Any> {
    val typeRef = object : TypeReference<HashMap<Any, Any>>() {}
    return jacksonObjectMapper.readValue(this, typeRef)
}

fun <T> T.objectToJson(): String =
    jacksonObjectMapper.writeValueAsString(this)

fun <T> T.objectToJsonNode(): JsonNode =
    jacksonObjectMapper.valueToTree<JsonNode>(this)

fun <T> List<T>.convertValue(): JsonNode =
    jacksonObjectMapper.convertValue(this, JsonNode::class.java)

fun String.stringToJsonNode(): JsonNode =
    jacksonObjectMapper.readTree(this)

fun Map<String, Any>.toJsonNode(): JsonNode =
    jacksonObjectMapper.valueToTree<JsonNode>(this)

fun <T : JsonNode> Map<String, Any>.valueToTree(): T =
    jacksonObjectMapper.valueToTree(this)

fun <T : JsonNode> List<Any>.valueToTree(): T =
    jacksonObjectMapper.valueToTree(this)

fun <T> InputStream.jsonToObject(t: Class<T>): T =
    jacksonObjectMapper.readValue(this, t)

fun InputStream.jsonToString(): String =
    jacksonObjectMapper.readTree(this).toString()

fun <T> InputStream.jsonToObject(typeReference: TypeReference<T>): T =
    jacksonObjectMapper.readValue(this, typeReference)

fun <T> ByteArray.jsonToObject(t: Class<T>): T =
    jacksonObjectMapper.readValue(this, t)

object JsonResourceUtils {

    fun <T> getPayload(resource: String, clazz: Class<T>): T {
        val resourceAsStream: InputStream = javaClass.classLoader.getResourceAsStream("payload/$resource")
        return resourceAsStream.jsonToObject(clazz)
    }

    fun getPayload(resource: String): String {
        val resourceAsStream: InputStream = javaClass.classLoader.getResourceAsStream("payload/$resource")
        return resourceAsStream.jsonToString()
    }

    fun <T> getPayload(resource: String, typeReference: TypeReference<T>): T {
        val resourceAsStream: InputStream = javaClass.classLoader.getResourceAsStream("payload/$resource")
        return resourceAsStream.jsonToObject(typeReference)
    }

}