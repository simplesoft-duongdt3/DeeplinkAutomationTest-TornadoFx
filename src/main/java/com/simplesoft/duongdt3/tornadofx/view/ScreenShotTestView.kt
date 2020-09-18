package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseView
import com.simplesoft.duongdt3.tornadofx.data.CompareImageService
import javafx.geometry.Pos
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import org.koin.core.get
import tornadofx.*
import java.io.File

class ScreenShotTestView : BaseView("Screenshot Automation test") {

    private var tv1st: Text? = null
    private var tv2nd: Text? = null

    private val screenShotTestViewModel = ScreenShotTestViewModel(
            coroutineScope = viewScope,
            appDispatchers = appDispatchers,
            fileOpener = get(),
            compareImageService = CompareImageService()
    )

    private val folder1stChange = ChangeListener<File> { _, _, newValue ->
        tv1st?.text = newValue.name
    }

    private val folder2stChange = ChangeListener<File> { _, _, newValue ->
        tv2nd?.text = newValue.name
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
        minWidth = 400.0
        paddingAll = 4.0

        hbox {
            prefHeight = 260.0
            vbox {
                prefWidth = 200.0
                alignment = Pos.CENTER
                paddingAll = 4.0
                tv1st = text {
                    alignment = Pos.CENTER
                    prefHeight = 40.0
                    isDisable = false
                    text = ""
                    text = "Drag 1st images folder\nto here"
                    textAlignment = TextAlignment.CENTER
                    style {
                        fontSize = 15.px
                    }
                }

                vbox {
                    prefHeight = 16.0
                }

                button {
                    alignment = Pos.CENTER
                    prefHeight = 24.0
                    text = "Browse folder"
                    action {
                        val choose1stDirectory = chooseDirectory(title = "Choose 1st images folder", initialDirectory = File(System.getProperty("user.dir")))
                        if (choose1stDirectory != null) {
                            screenShotTestViewModel.update1stDir(choose1stDirectory)
                        }
                    }
                }

                val thiz = this

                setOnDragOver {  event ->
                    val db = event.dragboard
                    if (event.gestureSource != thiz) {

                        if (db.hasFiles()) {
                            if (db.files.size == 1) {
                                val folder = db.files.first()
                                if (folder.isDirectory) {
                                    event.acceptTransferModes(*TransferMode.ANY);
                                }
                            }
                        }
                    }
                    event.consume();
                }


                setOnDragDropped { event ->
                    val db: Dragboard = event.dragboard
                    var success = false
                    if (db.hasFiles()) {

                        if (db.files.size == 1) {
                            val folder = db.files.first()
                            if (folder.isDirectory) {
                                screenShotTestViewModel.update1stDir(folder)
                                success = true
                            }
                        }
                    }
                    event.isDropCompleted = success
                    event.consume()
                }
            }

            vbox {
                prefWidth = 16.0
            }

            vbox {
                prefWidth = 200.0
                alignment = Pos.CENTER
                paddingAll = 4.0
                tv2nd = text {
                    alignment = Pos.CENTER
                    prefHeight = 40.0
                    isDisable = false
                    text = ""
                    text = "Drag 2nd images folder\nto here"
                    textAlignment = TextAlignment.CENTER

                    style {
                        fontSize = 15.px
                    }
                }

                vbox {
                    prefHeight = 16.0
                }

                button {
                    alignment = Pos.CENTER
                    prefHeight = 24.0
                    text = "Browse folder"
                    action {
                        val choose2ndDirectory = chooseDirectory(title = "Choose 1st images folder", initialDirectory = File(System.getProperty("user.dir")))
                        if (choose2ndDirectory != null) {
                            screenShotTestViewModel.update2ndDir(choose2ndDirectory)
                        }
                    }
                }
                val thiz = this

                setOnDragOver {  event ->
                    val db = event.dragboard
                    if (event.gestureSource != thiz) {

                        if (db.hasFiles()) {
                            if (db.files.size == 1) {
                                val folder = db.files.first()
                                if (folder.isDirectory) {
                                    event.acceptTransferModes(*TransferMode.ANY);
                                }
                            }
                        }
                    }
                    event.consume();
                }


                setOnDragDropped { event ->
                    val db: Dragboard = event.dragboard
                    var success = false
                    if (db.hasFiles()) {

                        if (db.files.size == 1) {
                            val folder = db.files.first()
                            if (folder.isDirectory) {
                                screenShotTestViewModel.update2ndDir(folder)
                                success = true
                            }
                        }
                    }
                    event.isDropCompleted = success
                    event.consume()
                }
            }
        }

        hbox {
            prefHeight = 16.0
        }

        hbox {
            button {
                textAlignment = TextAlignment.CENTER
                alignment = Pos.CENTER

                text = "Compare images"
                prefWidth = 400.0
                prefHeight = 60.0
                action {
                    screenShotTestViewModel.compareImages()
                }
            }
        }
    }
}