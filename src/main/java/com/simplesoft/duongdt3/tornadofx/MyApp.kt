package com.simplesoft.duongdt3.tornadofx


import com.simplesoft.duongdt3.tornadofx.data.di.dataModule
import com.simplesoft.duongdt3.tornadofx.helper.di.helperModule
import com.simplesoft.duongdt3.tornadofx.view.MainView
import com.simplesoft.duongdt3.tornadofx.view.di.viewModule
import javafx.stage.Stage
import org.koin.core.context.startKoin
import tornadofx.*

class MyApp: App(MainView::class, Styles::class) {
    override fun start(stage: Stage) {

        startKoin {
            modules(
                    dataModule,
                    helperModule,
                    viewModule
            )
        }

        stage.isResizable = false
        super.start(stage)
    }
}