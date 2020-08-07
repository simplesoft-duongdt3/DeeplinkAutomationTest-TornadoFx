package com.simplesoft.duongdt3.tornadofx.view.models

import java.io.File

data class TestCaseConfigFile(val file: File) {
    override fun toString(): String {
        return file.nameWithoutExtension
    }
}
