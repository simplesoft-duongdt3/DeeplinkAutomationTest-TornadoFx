package com.simplesoft.duongdt3.tornadofx.helper

import java.text.SimpleDateFormat
import java.util.*

fun Date.formatYYYYMMDD(): String {
    val formatter = SimpleDateFormat("yyyyMMdd")
    return formatter.format(this)
}


fun Date.formatyyyyMMddHHMM(): String {
    val formatter = SimpleDateFormat("yyyyMMddHHMM")
    return formatter.format(this)
}