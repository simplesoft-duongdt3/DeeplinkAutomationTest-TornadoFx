package com.simplesoft.duongdt3.tornadofx.helper.di

import com.simplesoft.duongdt3.tornadofx.helper.AppDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import org.koin.dsl.module

val helperModule = module {
    single {
        AppDispatchers(
                main = Dispatchers.JavaFx,
                io = Dispatchers.IO
        )
    }
}
