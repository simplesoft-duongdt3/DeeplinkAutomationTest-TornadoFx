package com.simplesoft.duongdt3.tornadofx.view.models

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.io.File

data class TestCaseStep(
        val index: Int,
        val id: String,
        val deepLinkText: String,
        val status: Status,
        val fileScreenshot: File?,
        val fileVideo: File?
) {
    val indexProperty = SimpleStringProperty("$index".padStart(3, '0'))
    val idProperty = SimpleStringProperty(id)
    val deepLinkTextProperty = SimpleStringProperty(deepLinkText)
    val statusProperty = SimpleObjectProperty(status)
    val fileScreenshotProperty = SimpleObjectProperty(fileScreenshot)
    val fileVideoProperty = SimpleObjectProperty(fileVideo)

    sealed class Status {
        object TODO : Status()
        object RUNNING : Status()
        data class SUCCESS(val timeMilis: Long) : Status()
        data class ERROR(val msg: String) : Status()
        object TIMEOUT : Status()
    }
}