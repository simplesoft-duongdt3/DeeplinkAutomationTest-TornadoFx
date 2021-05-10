package com.simplesoft.duongdt3.tornadofx.data

import com.google.gson.Gson
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfig
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfigInput
import com.simplesoft.duongdt3.tornadofx.data.models.EnvVarsConfigInput
import com.simplesoft.duongdt3.tornadofx.data.models.EnvironmentVars
import com.simplesoft.duongdt3.tornadofx.helper.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File

class ConfigParser: KoinComponent {
    private val gson = Gson()
    private val logger by inject<AppLogger>(qualifier = null)
    private val environmentVarsMerger by inject<EnvironmentVarsMerger>(qualifier = null)

    private val timeoutLoadingDefault = 3000L

    fun parse(configText: String, envVarsText: String): DeeplinkTestConfig? {
        val inputTrim = configText.trim()
        if (inputTrim.isNotBlank()) {
            try {
                val inputConfig = gson.fromJson(inputTrim, DeeplinkTestConfigInput::class.java)
                val envVarsConfigInput = gson.fromJson(inputTrim, EnvVarsConfigInput::class.java)
                return if (!inputConfig.deeplinks.isNullOrEmpty()) {
                    mapConfig(inputConfig, envVarsConfigInput)
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

    private fun mapConfig(inputConfig: DeeplinkTestConfigInput?, envVarsConfigInput: EnvVarsConfigInput): DeeplinkTestConfig {
        val vars = mapVars(envVarsConfigInput.environmentVars)
        return DeeplinkTestConfig(
                packageName = inputConfig?.packageName,
                timeoutLoadingMilis = inputConfig?.timeoutLoadingMilis.default(timeoutLoadingDefault),
                extraDeeplinkKey = inputConfig?.extraDeeplinkKey,
                deeplinkStartActivity = inputConfig?.deeplinkStartActivity,
                waitStartActivityDisappear = inputConfig?.waitStartActivityDisappear,
                mockServerUrl = environmentVarsMerger.getVar(key = "proxy_host_url", environmentVars = vars),
                deeplinks = inputConfig?.deeplinks.defaultEmpty().filterNotNull().filter { link ->
                    !link.deeplink.isNullOrBlank()
                }.mapIndexed { index, link ->
                    DeeplinkTestConfig.Deeplink(
                            activityName = link.activityName,
                            ignoreWaitStartActivity = link.ignoreWaitStartActivity.defaultFalse(),
                            id = link.id.default("${index + 1}".padStart(3, '0')),
                            mockServerRules = mapRules(link.rules, vars),
                            deeplink = link.deeplink.defaultEmpty()
                    )
                },
                environmentVars = vars
        )
    }

    private fun mapVars(varsInput: List<EnvVarsConfigInput.Var?>?): EnvironmentVars {
        val vars: List<EnvironmentVars.Var> = varsInput?.mapNotNull {
            return@mapNotNull EnvironmentVars.Var(
                    key = it?.key.defaultEmpty(),
                    value = it?.value.defaultEmpty()
            )
        }.defaultEmpty()

        return EnvironmentVars(vars = vars)
    }

    private fun mapRules(rules: List<DeeplinkTestConfigInput.Rule>?, vars: EnvironmentVars): List<DeeplinkTestConfig.Rule> {
        return rules.defaultEmpty().map { rule ->
            val requestConfigLines = File(rule.requestConfig.defaultEmpty()).readLines()
            val requestConfigBodyLines = requestConfigLines.subList(2, requestConfigLines.size)

            val responseConfigLines = File(rule.responseConfig.defaultEmpty()).readLines()
            val responseConfigBodyLines = responseConfigLines.subList(2, responseConfigLines.size)
            return@map DeeplinkTestConfig.Rule(
                    request = DeeplinkTestConfig.Rule.RequestConfig(
                            method = requestConfigLines.getOrNull(0).defaultEmpty(),
                            path = requestConfigLines.getOrNull(1).defaultEmpty(),
                            body = environmentVarsMerger.merge(contentText = linesToText(requestConfigBodyLines), environmentVars = vars)
                    ),
                    response = DeeplinkTestConfig.Rule.ResponseConfig(
                            statusCode = responseConfigLines.getOrNull(0)?.toIntOrNull().defaultZero(),
                            delayMillis = responseConfigLines.getOrNull(1)?.toLongOrNull().defaultZero(),
                            body = environmentVarsMerger.merge(contentText = linesToText(responseConfigBodyLines), environmentVars = vars)
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