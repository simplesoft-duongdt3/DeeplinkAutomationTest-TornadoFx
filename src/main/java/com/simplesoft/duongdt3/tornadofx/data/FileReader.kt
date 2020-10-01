package com.simplesoft.duongdt3.tornadofx.data

import com.simplesoft.duongdt3.tornadofx.helper.AppLogger
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset

class FileReader : KoinComponent {
    private val logger by inject<AppLogger>()

    fun readFile(file: File?): String {
        logger.log("Read file $file")

        return file?.readText(charset = Charsets.UTF_8).defaultEmpty()
    }
}