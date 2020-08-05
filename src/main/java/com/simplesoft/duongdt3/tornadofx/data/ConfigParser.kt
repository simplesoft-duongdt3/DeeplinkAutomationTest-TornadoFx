package com.simplesoft.duongdt3.tornadofx.data

import com.google.gson.Gson
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfig
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfigInput
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty

class ConfigParser {
    private val gson = Gson()

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
                        waitStartActivityDisappear = null,
                        deeplinks = deeplinks.filter { link -> link.isNotBlank() }.map { link ->
                            DeeplinkTestConfig.Deeplink(
                                    activityName = null,
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
                waitStartActivityDisappear = inputConfig?.waitStartActivityDisappear,
                deeplinks = inputConfig?.deeplinks.defaultEmpty().filterNotNull().filter { link ->
                    !link.deeplink.isNullOrBlank()
                }.map { link ->
                    DeeplinkTestConfig.Deeplink(
                            activityName = link.activityName,
                            deeplink = link.deeplink.defaultEmpty()
                    )
                }
        )
    }
}