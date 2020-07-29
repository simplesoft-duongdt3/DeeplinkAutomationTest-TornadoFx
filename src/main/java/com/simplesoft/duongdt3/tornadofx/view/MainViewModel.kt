package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseViewModel
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(coroutineScope: CoroutineScope): BaseViewModel(coroutineScope) {
    private var runTestJob: Job? = null
    fun runTest(textInput: String, isTakeScreenshot: Boolean, isRecordScreen: Boolean) {
        runTestJob?.cancel()

        runTestJob = viewModelScope.launch(Dispatchers.Main) {
            
        }
    }

    val texasCities = FXCollections.observableArrayList("Austin",
            "Dallas", "Midland", "San Antonio", "Fort Worth")
    val selectedCity = SimpleStringProperty("Dallas")
}