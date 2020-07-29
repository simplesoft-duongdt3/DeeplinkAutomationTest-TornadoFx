package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseView
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty
import javafx.geometry.Pos
import tornadofx.*

class MainView : BaseView("Deeplink Automation test") {

    private val mainViewModel = MainViewModel(viewScope)

    private lateinit var cbTakeScreenshot: javafx.scene.control.CheckBox
    private lateinit var cbRecordScreen: javafx.scene.control.CheckBox
    private lateinit var txtInput: javafx.scene.control.TextArea

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

            combobox<String>(mainViewModel.selectedCity, mainViewModel.texasCities) {
                minWidth = 150.0
            }
        }

        hbox {
            paddingAll = 4.0
            txtInput = textarea {

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
    }
}