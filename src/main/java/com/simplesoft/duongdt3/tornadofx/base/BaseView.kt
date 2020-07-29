package com.simplesoft.duongdt3.tornadofx.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.*

abstract class BaseView(title: String) : View(title) {
    protected val viewScope = CoroutineScope(SupervisorJob() + Dispatchers.JavaFx)

    override fun onUndock() {
        super.onUndock()
        viewScope.coroutineContext.cancel()
    }
}