package com.simplesoft.duongdt3.tornadofx.data

import com.google.gson.Gson
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfig
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfigInput
import com.simplesoft.duongdt3.tornadofx.helper.default
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty

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
                        deeplinks = deeplinks.filter { link -> link.isNotBlank() }.mapIndexed { index, link ->
                            DeeplinkTestConfig.Deeplink(
                                    id = "$index".padStart(3, '0'),
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
                timeoutLoadingMilis = inputConfig?.timeoutLoadingMilis.default(timeoutLoadingDefault),
                extraDeeplinkKey = inputConfig?.extraDeeplinkKey,
                deeplinkStartActivity = inputConfig?.deeplinkStartActivity,
                waitStartActivityDisappear = inputConfig?.waitStartActivityDisappear,
                deeplinks = inputConfig?.deeplinks.defaultEmpty().filterNotNull().filter { link ->
                    !link.deeplink.isNullOrBlank()
                }.mapIndexed { index, link ->
                    DeeplinkTestConfig.Deeplink(
                            activityName = link.activityName,
                            id = link.id.default("$index".padStart(3, '0')),
                            deeplink = link.deeplink.defaultEmpty()
                    )
                }
        )
    }
}