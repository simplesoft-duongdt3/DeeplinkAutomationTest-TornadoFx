package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseViewModel
import com.simplesoft.duongdt3.tornadofx.data.Either
import com.simplesoft.duongdt3.tornadofx.data.Failure
import com.simplesoft.duongdt3.tornadofx.helper.AppDispatchers
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice

class MainViewModel(coroutineScope: CoroutineScope, appDispatchers: AppDispatchers) : BaseViewModel(coroutineScope, appDispatchers) {

    private var selectedDevice: JadbDevice? = null
    private var devices = mutableListOf<JadbDevice>()

    val devicesText: ObservableList<String> = FXCollections.observableArrayList<String>()
    val selectedDeviceText = SimpleStringProperty()

    private var runTestJob: Job? = null
    private var getDevicesJob: Job? = null

    fun runTest(textInput: String, isTakeScreenshot: Boolean, isRecordScreen: Boolean) {
        runTestJob?.cancel()

        runTestJob = viewModelScope.launch(appDispatchers.main) {
            print("$textInput $isRecordScreen $isTakeScreenshot")
        }
    }

    fun requestInit() {
        getDevicesJob?.cancel()

        getDevicesJob = viewModelScope.launch(appDispatchers.main) {
            val result = withContext(appDispatchers.io) {
                getDevices()
            }

            result.either(
                    failAction = {
                        //TODO show error view
                    },
                    successAction = {
                        devices.clear()
                        devices.addAll(it)

                        devicesText.clear()
                        val list = devices.map { device ->
                            device.serial
                        }
                        devicesText.addAll(list)

                        selectedDevice = devices.firstOrNull()
                        selectedDeviceText.value = selectedDevice?.serial
                    }
            )
        }
    }

    private fun getDevices(): Either<Failure, List<JadbDevice>> {
        return try {
            val jadbConnection = JadbConnection()
            return Either.Success(jadbConnection.devices)
        } catch (ex: Exception) {
            Either.Fail(Failure.UnCatchError(ex))
        }

    }
}