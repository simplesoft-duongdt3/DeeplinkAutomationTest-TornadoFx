package com.simplesoft.duongdt3.tornadofx.data

import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfig
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Format
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import java.io.File
import java.util.concurrent.TimeUnit

class MockServerService {
    private var server: ClientAndServer? = null

    fun startServer() {
        server = ClientAndServer.startClientAndServer(9999)
    }

    fun updateConfig(mockServerConfig: MockServerConfig) {
        resetConfig()

        mockServerConfig.rules.forEach { rule ->
            addMockRule(rule)
        }
    }

    fun getApiLogs(): String? {
        return server?.retrieveRecordedRequestsAndResponses(
            null, Format.JSON
        )
    }

    private fun addMockRule(rule: DeeplinkTestConfig.Rule) {
        val request = rule.request
        val response = rule.response

        server?.`when`(
                request().withMethod(request.method)
                        .withPath(request.path).apply {
                            if (request.body.isNotBlank()) {
                                withBody(request.body)
                            }
                        }
        )?.respond(
                response()
                        .withContentType(MediaType.APPLICATION_JSON_UTF_8)
                        .withStatusCode(response.statusCode)
                        .withDelay(TimeUnit.MILLISECONDS, response.delayMillis).apply {
                            if (response.body.isNotBlank()) {
                                withBody(response.body)
                            }
                        }
        )
    }


    fun resetConfig() {
        server?.reset()
    }

    fun stopServer() {
        server?.stop()
    }

    data class MockServerConfig(val rules: List<DeeplinkTestConfig.Rule>) {
    }
}