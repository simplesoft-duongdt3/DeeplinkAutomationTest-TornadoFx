package com.simplesoft.duongdt3.tornadofx


import com.simplesoft.duongdt3.tornadofx.view.MainView
import javafx.stage.Stage
import tornadofx.App

class MyApp: App(MainView::class, Styles::class) {
    override fun start(stage: Stage) {
        stage.minHeight = 300.0
        stage.minWidth = 400.0
        stage.isResizable = false
        super.start(stage)
    }
}