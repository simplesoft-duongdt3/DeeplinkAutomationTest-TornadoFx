package com.simplesoft.duongdt3.tornadofx.helper


interface AppLogger {
    fun log(log: String)

    fun log(ex: Exception)
}