package com.simplesoft.duongdt3.tornadofx.helper

import kotlinx.coroutines.CoroutineDispatcher

class AppDispatchers(
        val main: CoroutineDispatcher,
        val io: CoroutineDispatcher
)