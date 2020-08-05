package com.simplesoft.duongdt3.tornadofx.data

import java.lang.Exception

sealed class Failure {
    data class UnCatchError(val exception: Exception) : Failure()
}