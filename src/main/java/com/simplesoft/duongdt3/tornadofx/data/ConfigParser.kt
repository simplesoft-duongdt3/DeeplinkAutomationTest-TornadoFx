package com.simplesoft.duongdt3.tornadofx.data

import com.google.gson.Gson
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfig
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfigInput
import com.simplesoft.duongdt3.tornadofx.helper.AppLogger
import com.simplesoft.duongdt3.tornadofx.helper.default
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty
import com.simplesoft.duongdt3.tornadofx.helper.defaultZero
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.qualifier
import java.io.File
import java.lang.StringBuilder

class ConfigParser: KoinComponent {
    private val gson = Gson()
    private val logger by inject<AppLogger>(qualifier = null)

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
                logger.log(e)
                return null
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