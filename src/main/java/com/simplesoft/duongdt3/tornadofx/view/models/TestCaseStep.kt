package com.simplesoft.duongdt3.tornadofx.view.models

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

data class TestCaseStep(
        val id: Int,
        val deepLinkText: String,
        val status: Status
) {
    val idProperty = SimpleIntegerProperty(id)
    val deepLinkTextProperty = SimpleStringProperty(deepLinkText)
    val statusProperty = SimpleObjectProperty(status)

    sealed class Status {
        object TODO : Status()
        object RUNNING : Status()
        data class DONE(val timeMilis: Long) : Status()
        data class ERROR(val msg: String) : Status()
        object TIMEOUT : Status()
    }
}