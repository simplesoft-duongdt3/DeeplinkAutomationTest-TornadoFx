package com.simplesoft.duongdt3.tornadofx.base

import com.simplesoft.duongdt3.tornadofx.helper.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinComponent

abstract class BaseViewModel(protected val viewModelScope: CoroutineScope, protected val appDispatchers: AppDispatchers): KoinComponent