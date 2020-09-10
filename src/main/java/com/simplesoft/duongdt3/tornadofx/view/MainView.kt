package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseView
import com.simplesoft.duongdt3.tornadofx.data.FileOpener
import com.simplesoft.duongdt3.tornadofx.helper.defaultFalse
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseConfigFile
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseDevice
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseStep
import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import javafx.scene.shape.StrokeType
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import org.koin.core.inject
import tornadofx.*
import java.io.File

class MainView : BaseView("Automation test") {


    override fun onDock() {
        super.onDock()
        currentStage?.width = 360.0
        currentStage?.height = 200.0

        this.currentWindow
    }

    override fun onUndock() {
        super.onUndock()
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
                    find<DeeplinkTestFragment>().openWindow(owner = this@MainView.currentWindow, resizable = false)
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