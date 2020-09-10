package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseView
import com.simplesoft.duongdt3.tornadofx.data.CompareImageService
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseStep
import javafx.geometry.Pos
import javafx.scene.control.TextField
import tornadofx.*
import java.io.File
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class ScreenShotTestView : BaseView("Screenshot Automation test") {

    private var tv1st: TextField? = null
    private var tv2st: TextField? = null
    private val screenShotTestViewModel = ScreenShotTestViewModel(viewScope, appDispatchers, CompareImageService())

    private val folder1stChange = ChangeListener<File> { _, _, newValue ->
        tv1st?.text = newValue.name
    }

    private val folder2stChange = ChangeListener<File> { _, _, newValue ->
        tv2st?.text = newValue.name
    }


    override fun onDock() {
        super.onDock()
        currentStage?.width = 400.0
        currentStage?.height = 320.0

        screenShotTestViewModel.current1stFolder.addListener(folder1stChange)
        screenShotTestViewModel.current2ndFolder.addListener(folder2stChange)
    }

    override fun onUndock() {
        super.onUndock()

        screenShotTestViewModel.current1stFolder.removeListener(folder1stChange)
        screenShotTestViewModel.current2ndFolder.removeListener(folder2stChange)
    }

    override val root = vbox {
        alignment = Pos.CENTER
        minWidth = 320.0
        paddingAll = 4.0
        hbox {
            alignment = Pos.CENTER
            paddingAll = 4.0
            tv1st = textfield {
                prefHeight = 40.0
                isDisable = true
                text = ""
                prefWidth = 250.0
                maxWidth = 250.0
                promptText = "Choose 1st images folder..."
            }

            hbox {
                prefWidth = 16.0
            }

            button {
                prefHeight = 40.0
                text = "Choose"
                action {
                    val choose1stDirectory = chooseDirectory(title = "Choose 1st images folder", initialDirectory = File(System.getProperty("user.dir")))
                    if (choose1stDirectory != null) {
                        screenShotTestViewModel.update1stDir(choose1stDirectory)
                    }
                }
            }
        }

        hbox {
            prefHeight = 16.0
        }

        hbox {
            alignment = Pos.CENTER
            paddingAll = 4.0
            tv2st = textfield {
                prefHeight = 40.0
                isDisable = true
                text = ""
                prefWidth = 250.0
                maxWidth = 250.0
                promptText = "Choose 2nd images folder..."
            }

            hbox {
                prefWidth = 16.0
            }

            button {
                prefHeight = 40.0
                text = "Choose"
                action {
                    val choose2ndDirectory = chooseDirectory(title = "Choose 1st images folder", initialDirectory = File(System.getProperty("user.dir")))
                    if (choose2ndDirectory != null) {
                        screenShotTestViewModel.update2ndDir(choose2ndDirectory)
                    }
                }
            }
        }

        hbox {
            prefHeight = 16.0
        }

        hbox {
            alignment = Pos.CENTER
            button {
                text = "Compare imgages"
                prefWidth = 320.0
                prefHeight = 40.0
                action {
                    screenShotTestViewModel.compareImages()
                }
            }
        }

    }
}