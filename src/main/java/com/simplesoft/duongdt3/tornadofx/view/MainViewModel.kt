package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseViewModel
import com.simplesoft.duongdt3.tornadofx.data.CmdExecutor
import com.simplesoft.duongdt3.tornadofx.data.Either
import com.simplesoft.duongdt3.tornadofx.data.Failure
import com.simplesoft.duongdt3.tornadofx.helper.AppDispatchers
import com.simplesoft.duongdt3.tornadofx.helper.AppLogger
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import org.koin.core.inject
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.RemoteFile
import java.io.ByteArrayOutputStream
import java.io.File


class MainViewModel(coroutineScope: CoroutineScope, appDispatchers: AppDispatchers) : BaseViewModel(coroutineScope, appDispatchers) {
    private val fileRoot = File(System.getProperty("user.dir"))
    private val logger by inject<AppLogger>()

    private var selectedDevice: JadbDevice? = null
    private var devices = mutableListOf<JadbDevice>()

    val devicesText: ObservableList<String> = FXCollections.observableArrayList<String>()
    val selectedDeviceText = SimpleStringProperty()

    private var runTestJob: Job? = null
    private var getDevicesJob: Job? = null

    init {
        logger.log("Root dir: $fileRoot")
    }
    fun runTest(textInput: String, isTakeScreenshot: Boolean, isRecordScreen: Boolean) {
        selectedDevice?.let { device ->
            if (device.state == JadbDevice.State.Device) {
                runTestJob?.cancel()

                runTestJob = viewModelScope.launch(appDispatchers.main) {
                    val resultDeeplinkTestCase = withContext(appDispatchers.io) {
                        runTestCaseFromInputDeeplinks(
                                textInput = textInput,
                                device = device,
                                takeScreenshot = isTakeScreenshot,
                                recordSreen = isRecordScreen
                        )
                    }

                    resultDeeplinkTestCase.either(
                            failAction = {
                                //TODO show error view
                                logger.log("resultTakeScreenshot fail $it")
                            },
                            successAction = {
                                //TODO success
                                logger.log("resultTakeScreenshot $it")
                            }
                    )


                }
            } else {
                //TODO show error when click device status != ONLINE
                logger.log("runTest error device state ${device.state}")
            }
        } ?: run {
            //TODO show error when click without device
            logger.log("runTest error without selected device")
        }
    }

    private suspend fun runTestCaseFromInputDeeplinks(textInput: String, device: JadbDevice, takeScreenshot: Boolean, recordSreen: Boolean): Either<Failure.UnCatchError, Boolean> {
        return try {
            val dirName = "deeplink_test_${System.currentTimeMillis()}"
            val dirTestCaseResult = File(fileRoot, dirName).apply {
                mkdirs()
            }

            val deeplinks = textInput.trim().lines()
            deeplinks.forEachIndexed { index, deeplink ->
                val filePrefix = "${index + 1}".padStart(3, '0')
                runDeeplinkTestCase(device, deeplink, filePrefix, takeScreenshot, recordSreen, dirTestCaseResult, fileRoot)
            }
            Either.Success(true)
        } catch (ex: Exception) {
            Either.Fail(Failure.UnCatchError(ex))
        }
    }

    private suspend fun runDeeplinkTestCase(device: JadbDevice, deeplink: String, filePrefix: String, takeScreenshot: Boolean, recordSreen: Boolean, dir: File, fileRoot: File) {
        val startTimeMilis = System.currentTimeMillis()
        goToDeviceHome(device)
        delay(500)
        val externalStoragePath = getDeviceStoragePath(device)
        var recordProccess: Process? = null
        val videoPathInDevice = "$externalStoragePath/screencap.mp4"
        if (recordSreen) {
            viewModelScope.launch(appDispatchers.io) {
                recordProccess = startRecordSreenByCmd(jadbDevice = device, videoPathInDevice = videoPathInDevice, fileRoot = fileRoot)
            }
            delay(500)
        }

        startDeeplink(jadbDevice = device, deeplink = deeplink)
        waitActivityDisplay(jadbDevice = device, activityName = "MainActivity")
        delay(4500)

        if (takeScreenshot) {
            takeScreenshot(jadbDevice = device, imgFileName = "${filePrefix}_screenshot.png", externalStoragePath = externalStoragePath, dir = dir)
        }

        if (recordSreen && recordProccess != null) {
            delay(1000)
            recordProccess?.destroy()
            delay(1000)
            stopRecordSreen(
                    jadbDevice = device,
                    imgFileName = "${filePrefix}_video.mp4",
                    imagePathInDevice = videoPathInDevice,
                    dir = dir
            )

        }

        val endTimeMilis = System.currentTimeMillis()
        logger.log("runDeeplinkTestCase $deeplink done after ${endTimeMilis - startTimeMilis}ms")
    }

    private fun startRecordSreenByCmd(jadbDevice: JadbDevice, videoPathInDevice: String, fileRoot: File): Process {
        val fileAdbPath = getAdbFilePath(fileRoot)
        logger.log("startRecordSreenByCmd $fileAdbPath")
        return Runtime.getRuntime().exec("\"$fileAdbPath\" -s ${jadbDevice.serial} shell screenrecord $videoPathInDevice")
    }

    private fun getAdbFilePath(fileRoot: File): String {
        val folderAdb = File(fileRoot, "adb")
        val fileAdb = if (isRunOnWindowsMachine()) {
            File(folderAdb, "adb.exe")
        } else {
            File(folderAdb, "adb")
        }

        val fileAdbPath = fileAdb.path
        return fileAdbPath
    }

    fun isRunOnWindowsMachine(): Boolean {
        return System.getProperty("os.name").startsWith("Windows")
    }

    private fun stopRecordSreen(jadbDevice: JadbDevice, imgFileName: String, imagePathInDevice: String, dir: File) {
        val startTimeMilis = System.currentTimeMillis()
        val saveFile = File(dir, imgFileName)
        jadbDevice.pull(RemoteFile(imagePathInDevice), saveFile)
        logger.log("stopRecordSreen save file $saveFile")

        val endTimeMilis = System.currentTimeMillis()
        logger.log("stopRecordSreen done after ${endTimeMilis - startTimeMilis}ms")
    }

    private suspend fun goToDeviceHome(device: JadbDevice) {
        delay(300)
        logger.log("goToDeviceHome")
        val deeplinkOut = ByteArrayOutputStream()
        device.executeShell(deeplinkOut, "input keyevent 3")
        delay(300)
    }

    private suspend fun waitActivityDisplay(jadbDevice: JadbDevice, activityName: String) {
        val startTimeMilis = System.currentTimeMillis()
        logger.log("waitActivityDisplay $activityName")

        var currentStep = 1
        val maxStep = 20

        var activitiesOutMsg = ""
        do {
            delay(1000)
            val activitiesOut = ByteArrayOutputStream()
            jadbDevice.executeShell(activitiesOut, "dumpsys activity activities")
            activitiesOutMsg = String(activitiesOut.toByteArray()).trim()
            currentStep++
        } while (!activitiesOutMsg.contains(activityName) && currentStep < maxStep)
        val endTimeMilis = System.currentTimeMillis()
        logger.log("waitActivityDisplay found $activityName after ${endTimeMilis - startTimeMilis}ms")

    }

    private fun startDeeplink(jadbDevice: JadbDevice, deeplink: String) {
        logger.log("startDeeplink start $deeplink")
        val deeplinkOut = ByteArrayOutputStream()
        jadbDevice.executeShell(deeplinkOut, "am start -a android.intent.action.VIEW -d '$deeplink'")
        val deeplinkOutMsg = String(deeplinkOut.toByteArray()).trim()
        logger.log("startDeeplink $deeplink $deeplinkOutMsg")
    }

    fun requestDevices() {
        getDevicesJob?.cancel()

        getDevicesJob = viewModelScope.launch(appDispatchers.main) {
            val result = withContext(appDispatchers.io) {
                startAdbServer(fileRoot)
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

    private fun startAdbServer(fileRoot: File) {
        logger.log("startAdbServer")
        val fileAdbPath = getAdbFilePath(fileRoot)
        val cmdExecutor = CmdExecutor()
        val executeCommand = cmdExecutor.executeCommand("$fileAdbPath start-server")
        logger.log("startAdbServer end $executeCommand")
    }

    private fun getDevices(): Either<Failure, List<JadbDevice>> {
        return try {
            val jadbConnection = JadbConnection()
            return Either.Success(jadbConnection.devices)
        } catch (ex: Exception) {
            Either.Fail(Failure.UnCatchError(ex))
        }
    }

    @Throws(Exception::class)
    private fun takeScreenshot(jadbDevice: JadbDevice, imgFileName: String, externalStoragePath: String, dir: File) {
        val startTimeMilis = System.currentTimeMillis()
        if (externalStoragePath.isNotBlank()) {
            val bout = ByteArrayOutputStream()
            val imagePathInDevice = "$externalStoragePath/screencap.png"
            jadbDevice.executeShell(bout, "screencap -p $imagePathInDevice")
            val resultScreenshot = String(bout.toByteArray())
            logger.log("resultScreenshot $resultScreenshot")

            val saveFile = File(dir, imgFileName)
            jadbDevice.pull(RemoteFile(imagePathInDevice), saveFile)
            logger.log("resultScreenshot save file $saveFile")
        }
        val endTimeMilis = System.currentTimeMillis()
        logger.log("takeScreenshot done after ${endTimeMilis - startTimeMilis}ms")
    }

    private fun getDeviceStoragePath(jadbDevice: JadbDevice): String {
        val storageOut = ByteArrayOutputStream()
        jadbDevice.executeShell(storageOut, "echo \$EXTERNAL_STORAGE")
        val externalStoragePath = String(storageOut.toByteArray()).trim()
        logger.log("found externalStoragePath $externalStoragePath")
        return externalStoragePath
    }
}