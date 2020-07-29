package com.simplesoft.duongdt3.tornadofx


import com.simplesoft.duongdt3.tornadofx.helper.di.helperModule
import com.simplesoft.duongdt3.tornadofx.view.MainView
import com.simplesoft.duongdt3.tornadofx.view.di.viewModule
import javafx.stage.Stage
import org.koin.core.context.startKoin
import tornadofx.App

class MyApp: App(MainView::class, Styles::class) {
    override fun start(stage: Stage) {

        startKoin {
            modules(
                    helperModule,
                    viewModule
            )
        }
        stage.minHeight = 300.0
        stage.minWidth = 400.0
        stage.isResizable = false
        super.start(stage)
    }
}