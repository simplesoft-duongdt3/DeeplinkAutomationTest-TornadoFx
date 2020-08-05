package com.simplesoft.duongdt3.tornadofx.base

import com.simplesoft.duongdt3.tornadofx.data.FileOpener
import com.simplesoft.duongdt3.tornadofx.helper.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.KoinComponent
import org.koin.core.inject
import tornadofx.*

abstract class BaseView(title: String) : View(title), KoinComponent {
    protected val appDispatchers: AppDispatchers by inject(qualifier = null)

    protected val viewScope = CoroutineScope(SupervisorJob() + appDispatchers.main)

    override fun onUndock() {
        super.onUndock()
        viewScope.coroutineContext.cancel()
    }
}