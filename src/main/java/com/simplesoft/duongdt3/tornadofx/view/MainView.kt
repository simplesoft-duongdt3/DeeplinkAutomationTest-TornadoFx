package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseView
import com.simplesoft.duongdt3.tornadofx.helper.defaultFalse
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseStep
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.TableView
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.*

class MainView : BaseView("Deeplink Automation test") {

    private val mainViewModel = MainViewModel(viewScope, appDispatchers)

    private var testCaseTableView: TableView<TestCaseStep>? = null
    private var cbTakeScreenshot: javafx.scene.control.CheckBox? = null
    private var cbRecordScreen: javafx.scene.control.CheckBox? = null

    private val listenerTestCaseSelected = ChangeListener<TestCaseStep> { _, _, newValue ->
        if (newValue != null) {
            testCaseTableView?.requestFocus()
            testCaseTableView?.selectWhere { testCase ->
                testCase == newValue
            }

            val indexOfTestCase = mainViewModel.processingSteps.indexOf(newValue)
            if (indexOfTestCase >= 0) {
                testCaseTableView?.scrollTo(indexOfTestCase)
            }
        }
    }

    private val listenerTestCaseStatus = ChangeListener<TestStatus> { valueOb, _, newValue ->
        if (newValue != null) {
            when (newValue) {
                TestStatus.ERROR_WITHOUT_CONFIG -> alert(type = Alert.AlertType.WARNING, header = "Warning", content = "Configs not found!")
                TestStatus.ERROR_WITHOUT_DEVICE -> alert(type = Alert.AlertType.WARNING, header = "Warning", content = "Android device not found!")
                TestStatus.ERROR -> alert(type = Alert.AlertType.ERROR, header = "Error", content = "Something wrong, check logs for more information.")
                TestStatus.DONE -> alert(type = Alert.AlertType.INFORMATION, header = "Finish", content = "Done test.")
            }
        }
    }

    override fun onDock() {
        super.onDock()
        mainViewModel.selectedTestCaseStep.addListener(listenerTestCaseSelected)
        mainViewModel.statusTest.addListener(listenerTestCaseStatus)
        mainViewModel.requestInit()
    }

    override fun onUndock() {
        super.onUndock()
        mainViewModel.statusTest.removeListener(listenerTestCaseStatus)
        mainViewModel.selectedTestCaseStep.removeListener(listenerTestCaseSelected)
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
            vbox {
                testCaseTableView = tableview(mainViewModel.processingSteps) {
                    isEditable = false
                    prefHeight = 300.0
                    fitToParentWidth()
                    column("Deeplink", TestCaseStep::deepLinkTextProperty).remainingWidth()

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

                    smartResize()
                }

                vbox {
                    prefHeight = 8.0
                }

                hbox {
                    alignment = Pos.BASELINE_CENTER

                    combobox<String>(mainViewModel.selectedFileConfigText, mainViewModel.fileConfigsText) {
                        minWidth = 150.0
                    }

                    vbox {
                        prefWidth = 16.0
                    }

                    button {
                        text = "Run"
                        prefWidth = 100.0
                        action {
                            mainViewModel.runTest(
                                    isTakeScreenshot = cbTakeScreenshot?.isSelected.defaultFalse(),
                                    isRecordScreen = cbRecordScreen?.isSelected.defaultFalse()
                            )
                        }
                    }
                }
            }
        }
    }
}