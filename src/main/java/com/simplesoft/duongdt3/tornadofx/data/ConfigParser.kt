package com.simplesoft.duongdt3.tornadofx.data

import com.google.gson.Gson
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfig
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfigInput
import com.simplesoft.duongdt3.tornadofx.helper.default
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty
import com.simplesoft.duongdt3.tornadofx.helper.defaultZero
import java.io.File
import java.lang.StringBuilder

class ConfigParser {
    private val gson = Gson()

    private val timeoutLoadingDefault = 3000L

    fun parse(configText: String): DeeplinkTestConfig? {
        val inputTrim = configText.trim()
        if (inputTrim.isNotBlank()) {
            try {
                val inputConfig = gson.fromJson(inputTrim, DeeplinkTestConfigInput::class.java)
                return if (!inputConfig.deeplinks.isNullOrEmpty()) {
                    mapConfig(inputConfig)
                } else {
                    null
                }
            } catch (e: Exception) {
                val deeplinks = inputTrim.trim().lines()
                return DeeplinkTestConfig(
                        packageName = null,
                        timeoutLoadingMilis = timeoutLoadingDefault,
                        waitStartActivityDisappear = null,
                        deeplinkStartActivity = null,
                        extraDeeplinkKey = null,
                        mockServerUrl = null,
                        deeplinks = deeplinks.filter { link -> link.isNotBlank() }.mapIndexed { index, link ->
                            DeeplinkTestConfig.Deeplink(
                                    id = "$index".padStart(3, '0'),
                                    activityName = null,
                                    mockServerRules = listOf(),
                                    deeplink = link
                            )
                        }
                )
            }
        } else {
            return null
        }
    }

    private fun mapConfig(inputConfig: DeeplinkTestConfigInput?): DeeplinkTestConfig {
        return DeeplinkTestConfig(
                packageName = inputConfig?.packageName,
                timeoutLoadingMilis = inputConfig?.timeoutLoadingMilis.default(timeoutLoadingDefault),
                extraDeeplinkKey = inputConfig?.extraDeeplinkKey,
                deeplinkStartActivity = inputConfig?.deeplinkStartActivity,
                waitStartActivityDisappear = inputConfig?.waitStartActivityDisappear,
                mockServerUrl = inputConfig?.mockserverUrl,
                deeplinks = inputConfig?.deeplinks.defaultEmpty().filterNotNull().filter { link ->
                    !link.deeplink.isNullOrBlank()
                }.mapIndexed { index, link ->
                    DeeplinkTestConfig.Deeplink(
                            activityName = link.activityName,
                            id = link.id.default("$index".padStart(3, '0')),
                            mockServerRules = mapRules(link.rules),
                            deeplink = link.deeplink.defaultEmpty()
                    )
                }
        )
    }

    private fun mapRules(rules: List<DeeplinkTestConfigInput.Rule>?): List<DeeplinkTestConfig.Rule> {
        return rules.defaultEmpty().map { rule ->
            val requestConfigLines = File(rule.requestConfig.defaultEmpty()).readLines()
            val requestConfigBodyLines = requestConfigLines.subList(2, requestConfigLines.size)

            val responseConfigLines = File(rule.responseConfig.defaultEmpty()).readLines()
            val responseConfigBodyLines = responseConfigLines.subList(2, responseConfigLines.size)
            return@map DeeplinkTestConfig.Rule(
                    request = DeeplinkTestConfig.Rule.RequestConfig(
                            method = requestConfigLines.getOrNull(0).defaultEmpty(),
                            path = requestConfigLines.getOrNull(1).defaultEmpty(),
                            body = linesToText(requestConfigBodyLines)
                    ),
                    response = DeeplinkTestConfig.Rule.ResponseConfig(
                            statusCode = responseConfigLines.getOrNull(0)?.toIntOrNull().defaultZero(),
                            delayMillis = responseConfigLines.getOrNull(1)?.toLongOrNull().defaultZero(),
                            body = linesToText(responseConfigBodyLines)
                    )
            )
        }
    }

    private fun linesToText(requestConfigBodyLines: List<String>): String {
        val stringBuilder = StringBuilder()
        requestConfigBodyLines.forEachIndexed { index, text ->
            if (index != 0) {
                stringBuilder.append(System.lineSeparator())
            }

            stringBuilder.append(text)
        }
        return stringBuilder.toString()
    }
}