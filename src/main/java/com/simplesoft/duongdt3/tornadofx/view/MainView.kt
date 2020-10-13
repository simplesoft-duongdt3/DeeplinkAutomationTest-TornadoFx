package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseView
import com.simplesoft.duongdt3.tornadofx.data.MockServerService
import javafx.geometry.Pos
import org.koin.core.inject
import tornadofx.*

class MainView : BaseView("Automation test") {

    override fun onDock() {
        super.onDock()
        currentStage?.width = 360.0
        currentStage?.height = 200.0
    }

    override val root = vbox {
        alignment = Pos.CENTER
        minWidth = 360.0
        paddingAll = 16.0
        vbox {
            paddingAll = 4.0
            prefHeight = 120.0

            button {
                text = "Deeplink test"
                prefHeight = 40.0
                prefWidth = 360.0
                action {
                    find<DeeplinkTestView>().openWindow(owner = this@MainView.currentWindow, resizable = false)
                }
            }

            vbox {
                prefHeight = 20.0
            }

            button {
                text = "Screenshot compare"
                prefHeight = 40.0
                prefWidth = 360.0
                action {
                    find<ScreenShotTestView>().openWindow(owner = this@MainView.currentWindow, resizable = false)
                }
            }

        }
    }
}