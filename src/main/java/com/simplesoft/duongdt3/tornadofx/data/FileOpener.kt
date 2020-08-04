package com.simplesoft.duongdt3.tornadofx.data

import com.simplesoft.duongdt3.tornadofx.helper.AppLogger
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.awt.Desktop
import java.io.File

class FileOpener: KoinComponent {
    private val logger: AppLogger by inject()

    fun openFile(filePath: String) {
        try {
            Desktop.getDesktop().open(File(filePath))
        } catch (e: Exception) {
            logger.log("openFile $filePath")
            logger.log(e)
        }
    }
}