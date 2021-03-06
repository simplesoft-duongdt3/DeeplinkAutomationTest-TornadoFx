package com.simplesoft.duongdt3.tornadofx


import com.simplesoft.duongdt3.tornadofx.data.MockServerService
import com.simplesoft.duongdt3.tornadofx.data.di.dataModule
import com.simplesoft.duongdt3.tornadofx.helper.di.helperModule
import com.simplesoft.duongdt3.tornadofx.view.MainView
import com.simplesoft.duongdt3.tornadofx.view.di.viewModule
import javafx.stage.Stage
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import tornadofx.*

class MyApp: App(MainView::class, Styles::class), KoinComponent {
    private val mockServerService: MockServerService by inject(qualifier = null)

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
        mockServerService.startServer()
    }

    override fun stop() {
        super.stop()
        mockServerService.stopServer()
    }
}