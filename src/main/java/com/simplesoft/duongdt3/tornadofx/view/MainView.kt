package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseView
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseStep
import javafx.geometry.Pos
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.*

class MainView : BaseView("Deeplink Automation test") {

    private val mainViewModel = MainViewModel(viewScope, appDispatchers)

    private lateinit var cbTakeScreenshot: javafx.scene.control.CheckBox
    private lateinit var cbRecordScreen: javafx.scene.control.CheckBox
    private lateinit var txtInput: javafx.scene.control.TextArea

    override fun onDock() {
        super.onDock()
        mainViewModel.requestDevices()
    }

    override val root = vbox {
        paddingAll = 4.0
        hbox {
            alignment = Pos.CENTER_LEFT
            paddingAll = 4.0
            cbTakeScreenshot = checkbox {
                text = "Take screnshot"
                isSelected = true
            }

            cbRecordScreen = checkbox {
                text = "Record screen"
                isSelected = true
            }

            hbox {
                prefWidth = 16.0
            }

            label {
                text = "Device"
            }

            combobox<String>(mainViewModel.selectedDeviceText, mainViewModel.devicesText) {
                minWidth = 150.0
            }
        }

        vbox {
            paddingAll = 4.0
            hbox {
                txtInput = textarea {
                    prefHeight = 200.0
                }

                spacer {
                    spacing = 4.0
                }
                vbox {

                    button {
                        text = "Run"
                        prefWidth = 75.0
                        prefHeight = 75.0
                        action {
                            mainViewModel.runTest(
                                    textInput = txtInput.text.defaultEmpty(),
                                    isTakeScreenshot = cbTakeScreenshot.isSelected,
                                    isRecordScreen = cbRecordScreen.isSelected
                            )
                        }
                    }

                    vbox {
                        prefHeight = 8.0
                    }

                    button {
                        text = "Cancel"
                        prefWidth = 75.0
                    }
                }
            }
            hbox {
                tableview(mainViewModel.processingSteps) {
                    fitToParentWidth()
                    column("Deeplink", TestCaseStep::deepLinkTextProperty)

                    column("Status", TestCaseStep::statusProperty) {
                        contentWidth(padding = 10.0)
                        cellFormat {
                            if( it != null ) {

                                val circle = Circle(10.0)
                                circle.stroke = Color.BLACK
                                circle.strokeWidth = 2.0

                                when(it) {
                                    TestCaseStep.Status.TODO -> {
                                        circle.fill = c("white")
                                        text = ""
                                    }
                                    TestCaseStep.Status.RUNNING -> {
                                        circle.fill = c("yellow")
                                        text = "Running"
                                    }
                                    is TestCaseStep.Status.DONE -> {
                                        circle.fill = c("green")
                                        text = "Done (${it.timeMilis}ms)"
                                    }
                                    is TestCaseStep.Status.ERROR -> {
                                        circle.fill = c("red")
                                        text = it.msg
                                    }
                                    TestCaseStep.Status.TIMEOUT -> {
                                        circle.fill = c("red")
                                        text = "Timeout"
                                        this.tooltip = Tooltip("Check logs for more information")
                                    }
                                }

                                graphic = circle
                            } else {
                                graphic = null
                                tooltip = null
                                text = null
                            }
                        }
                    }

                    columnResizePolicy = CONSTRAINED_RESIZE_POLICY
                }
            }
        }
    }
}