package com.simplesoft.duongdt3.tornadofx.data

import com.simplesoft.duongdt3.tornadofx.data.models.EnvironmentVars
import com.simplesoft.duongdt3.tornadofx.helper.AppLogger
import org.koin.core.KoinComponent
import org.koin.core.inject

class EnvironmentVarsMerger: KoinComponent {
    private val logger by inject<AppLogger>(qualifier = null)

    fun merge(contentText: String, environmentVars: EnvironmentVars): String {
        var newText = contentText
        logger.log("EnvironmentVarsMerger $environmentVars")
        environmentVars.vars.forEach { varItem ->
            newText = newText.replace("\$ENV{${varItem.key}}", varItem.value)
        }
        return newText
    }

    fun getVar(key: String, environmentVars: EnvironmentVars): String? {
        return environmentVars.vars.firstOrNull {
            it.key == key
        }?.value
    }

}