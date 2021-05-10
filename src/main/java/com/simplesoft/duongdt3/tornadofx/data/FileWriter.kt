package com.simplesoft.duongdt3.tornadofx.data

import com.simplesoft.duongdt3.tornadofx.helper.AppLogger
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File

class FileWriter : KoinComponent {
    private val logger by inject<AppLogger>()

    fun writeFile(file: File, text: String) {
        logger.log("WriteFile file ${file.name}")
        file.writeText(charset = Charsets.UTF_8, text = text)
    }

    fun writeFile(fileName: String, text: String) {
        logger.log("WriteFile path $fileName")
        val file = File(fileName)
        file.writeText(charset = Charsets.UTF_8, text = text)
    }

    fun appendFile(fileName: String, text: String) {
        logger.log("AppendFile $fileName")
        val file = File(fileName)
        file.appendText(charset = Charsets.UTF_8, text = text)
    }
}