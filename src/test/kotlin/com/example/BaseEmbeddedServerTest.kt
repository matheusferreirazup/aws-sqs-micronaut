package com.example

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer

abstract class BaseEmbeddedServerTest {

    private val configurationMap = testConfiguration()
    private val embeddedServer: EmbeddedServer = ApplicationContext.run(EmbeddedServer::class.java, configurationMap)
    protected val applicationContext: ApplicationContext = embeddedServer.applicationContext

    protected open fun testConfiguration(): Map<String, Any> {
        return mapOf()
    }
}