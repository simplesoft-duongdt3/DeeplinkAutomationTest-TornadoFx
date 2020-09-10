package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseView
import com.simplesoft.duongdt3.tornadofx.data.FileOpener
import com.simplesoft.duongdt3.tornadofx.helper.defaultFalse
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseConfigFile
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseDevice
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseStep
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableView
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import org.koin.core.inject
import tornadofx.*

class MainView : BaseView("Deeplink Automation test") {

    protected val fileOpener: FileOpener by inject(qualifier = null)

    private var progressIndicator: ProgressIndicator? = null
    private var resultTextView: Text? = null
    private val mainViewModel = MainViewModel(viewScope, appDispatchers)

    private var testCaseTableView: TableView<TestCaseStep>? = null
    private var cbTakeScreenshot: javafx.scene.control.CheckBox? = null
    private var cbRecordScreen: javafx.scene.control.CheckBox? = null

    private val listenerProcessing = ListChangeListener<TestCaseStep> { newValue ->
        val steps = newValue.list
        val runningCount = steps.count { step ->
            step.status == TestCaseStep.Status.RUNNING
        }
        val doneCount = steps.count { step ->
            step.status != TestCaseStep.Status.TODO && step.status != TestCaseStep.Status.RUNNING
        }
        val errorCount = steps.count { step ->
            step.status is TestCaseStep.Status.ERROR || step.status == TestCaseStep.Status.TIMEOUT
        }
        val successCount = steps.count { step ->
            step.status is TestCaseStep.Status.SUCCESS
        }

        val totalCount = steps.size

        resultTextView?.text = "Result: $doneCount/$totalCount ($errorCount Error, $successCount Success)"

        if (runningCount > 0) {
            progressIndicator?.show()
        } else {
            progressIndicator?.hide()
        }
    }

    private val listenerTestCaseSelected = ChangeListener<TestCaseStep> { _, _, newValue ->
        if (newValue != null) {
            testCaseTableView?.requestFocus()
            testCaseTableView?.selectWhere { testCase ->
                testCase == newValue
            }
        }
    }

    private val listenerTestCaseStatus = ChangeListener<TestStatus> { _, _, newValue ->
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
        mainViewModel.processingSteps.addListener(listenerProcessing)
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
        minWidth = 680.0
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

            combobox<TestCaseDevice>(mainViewModel.selectedDevice, mainViewModel.devices) {
                minWidth = 150.0
            }
        }

        vbox {
            paddingAll = 4.0
            vbox {
                stackpane {
                    testCaseTableView = tableview(mainViewModel.processingSteps) {
                        isPickOnBounds = true
                        placeholder = text("No content in testcase list.\r\nSelect file config + device >> Run.") {
                            textAlignment = TextAlignment.CENTER
                        }
                        isEditable = false
                        prefHeight = 300.0
                        fitToParentWidth()
                        column("#", TestCaseStep::indexProperty) {
                            minWidth = 20.0
                        }

                        column("ID", TestCaseStep::idProperty) {
                            minWidth = 30.0
                        }
                        column("Deeplink", TestCaseStep::deepLinkTextProperty).remainingWidth()

                        column("Status", TestCaseStep::statusProperty) {
                            contentWidth(padding = 10.0)
                            minWidth = 150.0
                            cellFormat {
                                if (it != null) {

                                    val circle = Circle(10.0)
                                    circle.stroke = Color.BLACK
                                    circle.strokeWidth = 2.0

                                    when (it) {
                                        TestCaseStep.Status.TODO -> {
                                            circle.fill = c("white")
                                            text = "Todo"
                                        }
                                        TestCaseStep.Status.RUNNING -> {
                                            circle.fill = c("yellow")
                                            text = "Running"
                                        }
                                        is TestCaseStep.Status.SUCCESS -> {
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

                        column("Image", TestCaseStep::fileScreenshotProperty) {
                            contentWidth(padding = 10.0)
                            cellFormat { file ->
                                if (file != null && file.exists()) {
                                    graphic = text("   View") {
                                        fitToParentWidth()
                                        textAlignment = TextAlignment.CENTER
                                        style {
                                            textFill = Color.RED
                                            backgroundColor += Color.RED
                                        }
                                        setOnMouseClicked {
                                            fileOpener.openFile(file)
                                        }
                                    }
                                } else {
                                    graphic = null
                                    tooltip = null
                                    text = null
                                }
                            }
                        }

                        column("Video", TestCaseStep::fileVideoProperty) {
                            contentWidth(padding = 10.0)
                            cellFormat { file ->
                                if (file != null && file.exists()) {
                                    graphic = text("   View") {
                                        style {
                                            baseColor = Color.ALICEBLUE
                                        }
                                        setOnMouseClicked {
                                            fileOpener.openFile(file)
                                        }
                                    }
                                } else {
                                    graphic = null
                                    tooltip = null
                                    text = null
                                }
                            }
                        }

                        smartResize()
                    }

                    hbox {
                        isPickOnBounds = false
                        alignment = Pos.CENTER
                        prefHeight = 100.0
                        prefWidth = 100.0

                        progressIndicator = progressindicator {
                            hide()
                        }
                    }
                }

                vbox {
                    prefHeight = 8.0
                }

                hbox {
                    fitToParentWidth()
                    alignment = Pos.CENTER
                    resultTextView = text {
                        text = "Let's do a deeplink test now."
                        paddingAll = 8.0
                        textAlignment = TextAlignment.CENTER
                        style {
                            fontSize = 18.px
                        }
                    }
                }

                vbox {
                    prefHeight = 8.0
                }

                hbox {
                    alignment = Pos.BASELINE_CENTER

                    combobox<TestCaseConfigFile>(mainViewModel.selectedConfigFile, mainViewModel.configFiles) {
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