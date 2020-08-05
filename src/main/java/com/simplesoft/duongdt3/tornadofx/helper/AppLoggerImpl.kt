package com.simplesoft.duongdt3.tornadofx.helper

import java.io.PrintWriter
import java.io.StringWriter


class AppLoggerImpl: AppLogger {
    override fun log(log: String) {
        println(log)
    }

    override fun log(ex: Exception) {
        val errors = StringWriter()
        ex.printStackTrace(PrintWriter(errors))
        println("----------------Exception------------------")
        println("$ex")
        println(errors.toString())
        println("----------------End of Exception------------------")
    }
}