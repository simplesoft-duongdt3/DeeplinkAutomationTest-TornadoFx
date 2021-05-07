package com.simplesoft.duongdt3.tornadofx.data.di

import com.simplesoft.duongdt3.tornadofx.data.*
import com.simplesoft.duongdt3.tornadofx.helper.AppDispatchers
import com.simplesoft.duongdt3.tornadofx.helper.AppLogger
import com.simplesoft.duongdt3.tornadofx.helper.AppLoggerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import org.koin.dsl.module

val dataModule = module {
    single {
        ConfigParser()
    }

    single {
        FileReader()
    }


    single {
        FileWriter()
    }

    single {
        FileOpener()
    }

    single {
        MockServerService()
    }
}
