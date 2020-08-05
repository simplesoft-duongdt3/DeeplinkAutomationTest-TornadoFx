package com.simplesoft.duongdt3.tornadofx.data

import com.simplesoft.duongdt3.tornadofx.helper.AppLogger
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class FileReader : KoinComponent {
    private val logger by inject<AppLogger>()

    fun readFile(file: File?): String {
        logger.log("Read file $file")
        val contents = StringBuffer()
        if (file != null && file.exists()) {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader(file))
                var text: String? = null

                // repeat until all lines is read
                while (reader.readLine().also { text = it } != null) {
                    contents.append(text).append(System.lineSeparator())
                }
            } catch (e: Exception) {
                logger.log(e)
            } finally {
                reader?.close()
            }
        }

        return contents.toString()
    }
}