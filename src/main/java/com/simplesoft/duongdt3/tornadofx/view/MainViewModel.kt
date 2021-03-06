package com.simplesoft.duongdt3.tornadofx.view

import com.simplesoft.duongdt3.tornadofx.base.BaseViewModel
import com.simplesoft.duongdt3.tornadofx.data.*
import com.simplesoft.duongdt3.tornadofx.data.models.DeeplinkTestConfig
import com.simplesoft.duongdt3.tornadofx.helper.AppDispatchers
import com.simplesoft.duongdt3.tornadofx.helper.AppLogger
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseConfigFile
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseDevice
import com.simplesoft.duongdt3.tornadofx.view.models.TestCaseStep
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import org.koin.core.inject
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.RemoteFile
import tornadofx.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.StringBuilder
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainViewModel(coroutineScope: CoroutineScope, appDispatchers: AppDispatchers) : BaseViewModel(coroutineScope, appDispatchers) {
    private val fileRoot = File(System.getProperty("user.dir"))
    private val logger by inject<AppLogger>()
    private val fileReader by inject<FileReader>()
    private val fileWriter by inject<FileWriter>()
    private val configParser by inject<ConfigParser>()
    private val mockServerService by inject<MockServerService>()

    val statusTest = SimpleObjectProperty<TestStatus>()

    val devices = FXCollections.observableArrayList<TestCaseDevice>()
    val selectedDevice = SimpleObjectProperty<TestCaseDevice>()
    val selectedTestCaseStep = SimpleObjectProperty<TestCaseStep>()

    val configFiles: ObservableList<TestCaseConfigFile> = FXCollections.observableArrayList<TestCaseConfigFile>()
    val selectedConfigFile = SimpleObjectProperty<TestCaseConfigFile>().apply {
        onChange { file ->
            onFileSelected(file)
        }
    }

    private fun onFileSelected(file: TestCaseConfigFile?) {
        viewModelScope.launch(appDispatchers.main) {
            if (file != null) {
                withContext(appDispatchers.io) {
                    val configText = fileReader.readFile(file.file)
                    val envVarsText = fileReader.readFile(getEnvVarsFile())
                    val deeplinkTestConfig = configParser.parse(configText = configText, envVarsText = envVarsText)
                    if (deeplinkTestConfig != null) {
                        val deeplinks = deeplinkTestConfig.deeplinks
                        initDeeplinkTestCaseSteps(deeplinks)
                    } else {
                        initDeeplinkTestCaseSteps(listOf())
                    }
                }
            } else {
                initDeeplinkTestCaseSteps(listOf())
            }
        }
    }

    private fun getEnvVarsFile(): File? {
        val folderConfigs = File(File(fileRoot, "configs"), "env_vars")
        return folderConfigs.listFiles()?.toList()?.firstOrNull()
    }

    val processingSteps: ObservableList<TestCaseStep> = FXCollections.observableArrayList<TestCaseStep>()

    private var runTestJob: Job? = null
    private var getDevicesJob: Job? = null
    private var getFileConfigJob: Job? = null

    init {
        logger.log("Root dir: $fileRoot")
    }

    fun runTest(isTakeScreenshot: Boolean, isRecordScreen: Boolean) {
        val device = selectedDevice.value
        if (device == null) {
            statusTest.value = null
            statusTest.value = TestStatus.ERROR_WITHOUT_DEVICE
            logger.log("runTest error without selected device")
            return
        }

        val file = selectedConfigFile.value
        if (file == null) {
            statusTest.value = null
            statusTest.value = TestStatus.ERROR_WITHOUT_CONFIG
            logger.log("runTest error without selected config file")
            return
        }

        runTestJob?.cancel()
        runTestJob = viewModelScope.launch(appDispatchers.main) {
            processingSteps.clear()
            val resultDeeplinkTestCase = withContext(appDispatchers.io) {
                val configText = fileReader.readFile(file.file)
                val envVarsText = fileReader.readFile(getEnvVarsFile())
                runTestCaseFromInputDeeplinks(
                        configText = configText,
                        envVarsText = envVarsText,
                        device = device.device,
                        takeScreenshot = isTakeScreenshot,
                        recordSreen = isRecordScreen
                )
            }

            resultDeeplinkTestCase.either(
                    failAction = {
                        statusTest.value = null
                        statusTest.value = TestStatus.ERROR
                        logger.log("resultTakeScreenshot fail $it")
                    },
                    successAction = {
                        statusTest.value = null
                        statusTest.value = TestStatus.DONE
                        logger.log("resultTakeScreenshot $it")
                    }
            )

            logger.log("getApiLogs all ${mockServerService.getApiLogs()}")
        }
    }

    private suspend fun runTestCaseFromInputDeeplinks(
            configText: String,
            envVarsText: String,
            device: JadbDevice,
            takeScreenshot: Boolean,
            recordSreen: Boolean
    ): Either<Failure.UnCatchError, Boolean> {
        return try {
            val deeplinkTestConfig = configParser.parse(configText = configText, envVarsText = envVarsText)
            if (deeplinkTestConfig != null) {
                val dirName = "deeplink_test_${System.currentTimeMillis()}"
                val dirTestCaseResult = File(fileRoot, dirName).apply {
                    mkdirs()
                }

                val deeplinks = deeplinkTestConfig.deeplinks

                initDeeplinkTestCaseSteps(deeplinks)

                clearDataApp(device, deeplinkTestConfig)

                deeplinks.forEachIndexed { index, deeplink ->
                    runDeeplinkTestCase(
                            id = deeplink.id,
                            mockServerRules = deeplink.mockServerRules,
                            waitStartActivityDisappear = deeplinkTestConfig.waitStartActivityDisappear,
                            device = device,
                            deeplink = deeplink.deeplink,
                            deeplinkWaitActivity = deeplink.activityName,
                            takeScreenshot = takeScreenshot,
                            recordSreen = recordSreen,
                            dirTestCase = dirTestCaseResult,
                            fileRoot = fileRoot,
                            ignoreWaitStartActivity = deeplink.ignoreWaitStartActivity,
                            deeplinkStartActivity = deeplinkTestConfig.deeplinkStartActivity,
                            extraDeeplinkKey = deeplinkTestConfig.extraDeeplinkKey,
                            packageName = deeplinkTestConfig.packageName,
                            timeoutLoadingMilis = deeplinkTestConfig.timeoutLoadingMilis
                    )
                }

                Either.Success(true)
            } else {
                Either.Fail(Failure.UnCatchError(Exception()))
            }
        } catch (ex: Exception) {
            Either.Fail(Failure.UnCatchError(ex))
        }
    }

    private fun clearDataApp(device: JadbDevice, deeplinkTestConfig: DeeplinkTestConfig) {
        val clearAppOut = ByteArrayOutputStream()
        device.executeShell(clearAppOut, "pm clear '${deeplinkTestConfig.packageName}'")
        val clearAppOutMsg = String(clearAppOut.toByteArray()).trim()

        logger.log("clearApp ${deeplinkTestConfig.packageName} $clearAppOutMsg")
    }

    private fun initDeeplinkTestCaseSteps(deeplinks: List<DeeplinkTestConfig.Deeplink>) {
        viewModelScope.launch(appDispatchers.main) {
            processingSteps.clear()
            processingSteps.addAll(mapDeepLinkSteps(deeplinks))
        }
    }

    private fun mapDeepLinkSteps(deeplinks: List<DeeplinkTestConfig.Deeplink>): List<TestCaseStep> {
        return deeplinks.mapIndexed { index, deeplink ->
            TestCaseStep(
                    index = index,
                    id = deeplink.id,
                    deepLinkText = deeplink.deeplink,
                    status = TestCaseStep.Status.TODO,
                    fileScreenshot = null,
                    fileVideo = null
            )
        }
    }

    private suspend fun runDeeplinkTestCase(
            waitStartActivityDisappear: String?,
            device: JadbDevice,
            deeplink: String,
            deeplinkWaitActivity: String?,
            takeScreenshot: Boolean,
            recordSreen: Boolean,
            dirTestCase: File,
            fileRoot: File,
            id: String,
            packageName: String?,
            deeplinkStartActivity: String?,
            ignoreWaitStartActivity: Boolean,
            extraDeeplinkKey: String?,
            timeoutLoadingMilis: Long,
            mockServerRules: List<DeeplinkTestConfig.Rule>
    ) {
        fireEventTestCaseRunning(id)
        val startTimeMilis = System.currentTimeMillis()
        mockServerService.updateConfig(MockServerService.MockServerConfig(rules = mockServerRules))

        goToDeviceHome(device)
        delay(500)
        val externalStoragePath = getDeviceStoragePath(device)
        var recordProccess: Process? = null
        val videoPathInDevice = "$externalStoragePath/screencap.mp4"
        if (recordSreen) {
            viewModelScope.launch(appDispatchers.io) {
                recordProccess = startRecordSreenByCmd(
                        jadbDevice = device,
                        videoPathInDevice = videoPathInDevice,
                        fileRoot = fileRoot
                )
            }
            delay(500)
        }

        val imgFileName = "${id}_screenshot.png"
        val apiLogFileName = "${id}_api_logs.json"
        val videoFileName = "${id}_video.mp4"
        val fileScreenshot = File(dirTestCase, imgFileName)
        val fileVideo = File(dirTestCase, videoFileName)
        val fileApiLogs = File(dirTestCase, apiLogFileName)
        val startDeeplinkSuccess = startDeeplink(
                id = id,
                jadbDevice = device,
                deeplink = deeplink,
                deeplinkWaitActivity = deeplinkWaitActivity,
                waitStartActivityDisappear = waitStartActivityDisappear,
                packageName = packageName,
                ignoreWaitStartActivity = ignoreWaitStartActivity,
                extraDeeplinkKey = extraDeeplinkKey,
                deeplinkStartActivity = deeplinkStartActivity,
                timeoutLoadingMilis = timeoutLoadingMilis
        )

        if (takeScreenshot) {
            takeScreenshot(
                    jadbDevice = device,
                    fileScreenshot = fileScreenshot,
                    externalStoragePath = externalStoragePath
            )
        }

        if (recordSreen && recordProccess != null) {
            delay(1000)
            recordProccess?.destroy()
            delay(1000)
            stopRecordSreen(
                    jadbDevice = device,
                    fileVideo = fileVideo,
                    imagePathInDevice = videoPathInDevice
            )
        }

        fileWriter.writeFile(file = fileApiLogs, text = mockServerService.getApiLogs().defaultEmpty())

        val endTimeMilis = System.currentTimeMillis()
        val workingTime = endTimeMilis - startTimeMilis
        if (startDeeplinkSuccess) {
            fireEventTestCaseDone(id = id, milis = workingTime)
        }

        fireEventTestCaseUpdateFileResult(id = id, fileScreenshot = fileScreenshot, fileVideo = fileVideo)
        logger.log("runDeeplinkTestCase $deeplink done after ${workingTime}ms")

    }

    private fun fireEventTestCaseUpdateFileResult(id: String, fileScreenshot: File, fileVideo: File) {
        viewModelScope.launch(appDispatchers.main) {
            val indexTestCaseStep = processingSteps.indexOfFirst {
                it.id == id
            }

            val testCaseStep = processingSteps.getOrNull(indexTestCaseStep)
            if (testCaseStep != null) {
                val testCaseStepCopy = testCaseStep.copy(fileScreenshot = fileScreenshot, fileVideo = fileVideo)
                processingSteps[indexTestCaseStep] = testCaseStepCopy
            }
        }
    }

    private fun fireEventTestCaseDone(id: String, milis: Long) {
        viewModelScope.launch(appDispatchers.main) {
            val indexTestCaseStep = processingSteps.indexOfFirst {
                it.id == id
            }

            val testCaseStep = processingSteps.getOrNull(indexTestCaseStep)
            if (testCaseStep != null) {
                val testCaseStepCopy = testCaseStep.copy(status = TestCaseStep.Status.SUCCESS(milis))
                processingSteps[indexTestCaseStep] = testCaseStepCopy
            }
        }
    }

    private fun fireEventTestCaseRunning(id: String) {
        viewModelScope.launch(appDispatchers.main) {
            val indexTestCaseStep = processingSteps.indexOfFirst {
                it.id == id
            }

            val testCaseStep = processingSteps.getOrNull(indexTestCaseStep)
            if (testCaseStep != null) {
                val testCaseStepCopy = testCaseStep.copy(status = TestCaseStep.Status.RUNNING)
                processingSteps[indexTestCaseStep] = testCaseStepCopy

                selectedTestCaseStep.value = testCaseStepCopy
            }
        }
    }

    private fun startRecordSreenByCmd(jadbDevice: JadbDevice, videoPathInDevice: String, fileRoot: File): Process? {
        try {
            logger.log("startRecordSreenByCmd $fileRoot")
            val fileAdbPath = getAdbFilePath(fileRoot)
            logger.log("startRecordSreenByCmd $fileAdbPath")
            return Runtime.getRuntime().exec("\"$fileAdbPath\" -s ${jadbDevice.serial} shell screenrecord $videoPathInDevice")
        } catch (e: Exception) {
            logger.log(e)
            return null
        }
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

    private fun stopRecordSreen(jadbDevice: JadbDevice, imagePathInDevice: String, fileVideo: File) {
        val startTimeMilis = System.currentTimeMillis()
        jadbDevice.pull(RemoteFile(imagePathInDevice), fileVideo)
        logger.log("stopRecordSreen save file $fileVideo")

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

    private suspend fun waitActivityDisplay(jadbDevice: JadbDevice, activityName: String): Boolean {
        val startTimeMilis = System.currentTimeMillis()
        logger.log("waitActivityDisplay $activityName")

        var currentStep = 1
        val maxStep = 20
        var isFoundActivity: Boolean
        var isTimeout: Boolean
        var activitiesOutMsg: String
        do {
            delay(1000)
            val activitiesOut = ByteArrayOutputStream()
            jadbDevice.executeShell(activitiesOut, "dumpsys activity activities")
            activitiesOutMsg = String(activitiesOut.toByteArray()).trim()

            logger.log("dumpsys activity activities")
            logger.log(activitiesOutMsg)
            currentStep++
            isFoundActivity = findActivityInStacktrace(activityName = activityName, activitiesStacktrace = activitiesOutMsg)
            isTimeout = currentStep >= maxStep
        } while (!isFoundActivity && !isTimeout)
        val endTimeMilis = System.currentTimeMillis()
        logger.log("waitActivityDisplay found $activityName after ${endTimeMilis - startTimeMilis}ms")
        return isFoundActivity
    }

    private fun findActivityInStacktrace(activityName: String, activitiesStacktrace: String): Boolean {
        val p: Pattern = Pattern.compile("(Run).*#\\d+.*(ActivityRecord).*(\\.$activityName)")
        val m: Matcher = p.matcher(activitiesStacktrace)
        val find = m.find()
        logger.log("findActivityInStacktrace $activityName found = $find")
        return find
    }

    private suspend fun waitActivityDisappear(jadbDevice: JadbDevice, activityName: String): Boolean {
        val startTimeMilis = System.currentTimeMillis()
        logger.log("waitActivityDisappear $activityName")

        var currentStep = 1
        val maxStep = 20
        var isFoundActivity: Boolean
        var isTimeout: Boolean
        var activitiesOutMsg: String
        do {
            delay(1000)
            val activitiesOut = ByteArrayOutputStream()
            jadbDevice.executeShell(activitiesOut, "dumpsys activity activities")
            activitiesOutMsg = String(activitiesOut.toByteArray()).trim()

            logger.log("dumpsys activity activities")
            logger.log(activitiesOutMsg)
            currentStep++

            isFoundActivity = findActivityInStacktrace(activityName = activityName, activitiesStacktrace = activitiesOutMsg)
            isTimeout = currentStep >= maxStep
        } while (isFoundActivity && !isTimeout)
        val endTimeMilis = System.currentTimeMillis()
        logger.log("waitActivityDisappear $activityName found = $isFoundActivity after ${endTimeMilis - startTimeMilis}ms")
        return !isFoundActivity
    }

    private suspend fun startDeeplink(
            id: String,
            jadbDevice: JadbDevice,
            deeplink: String,
            deeplinkWaitActivity: String?,
            waitStartActivityDisappear: String?,
            packageName: String?,
            deeplinkStartActivity: String?,
            ignoreWaitStartActivity: Boolean,
            extraDeeplinkKey: String?,
            timeoutLoadingMilis: Long
    ): Boolean {
        try {
            logger.log("startDeeplink start $deeplink")

            val waitActivityDisplay = if (deeplinkStartActivity != null && extraDeeplinkKey != null && packageName != null) {
                startDeeplinkWithActivity(
                        jadbDevice = jadbDevice,
                        deeplink = deeplink,
                        deeplinkStartActivity = deeplinkStartActivity,
                        extraDeeplinkKey = extraDeeplinkKey,
                        packageName = packageName,
                        deeplinkWaitActivity = deeplinkWaitActivity,
                        waitStartActivityDisappear = waitStartActivityDisappear
                )
            } else {
                startDeeplinkWithWebAction(
                        jadbDevice = jadbDevice,
                        deeplink = deeplink,
                        deeplinkWaitActivity = deeplinkWaitActivity,
                        ignoreWaitStartActivity = ignoreWaitStartActivity,
                        waitStartActivityDisappear = waitStartActivityDisappear
                )
            }

            //time for loading
            delay(timeoutLoadingMilis)
            if (!waitActivityDisplay) {
                fireEventTestCaseTimeout(id)
            }
            logger.log("startDeeplink done")
            return waitActivityDisplay
        } catch (e: Exception) {
            logger.log("startDeeplink error")
            fireEventTestCaseError(id, "Error")
            logger.log(e)
            return false
        }
    }

    private suspend fun startDeeplinkWithWebAction(
            jadbDevice: JadbDevice,
            deeplink: String,
            ignoreWaitStartActivity: Boolean,
            waitStartActivityDisappear: String?,
            deeplinkWaitActivity: String?
    ): Boolean {
        val deeplinkOut = ByteArrayOutputStream()
        // -W wait
        jadbDevice.executeShell(deeplinkOut, "am start -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d '$deeplink'")
        val deeplinkOutMsg = String(deeplinkOut.toByteArray()).trim()

        logger.log("startDeeplink $deeplink $deeplinkOutMsg")
        var waitActivityDisplay = true
        if (!ignoreWaitStartActivity && !waitStartActivityDisappear.isNullOrBlank()) {
            waitActivityDisplay = waitActivityDisappear(jadbDevice = jadbDevice, activityName = waitStartActivityDisappear)
            if (waitActivityDisplay) {
                if (!deeplinkWaitActivity.isNullOrBlank()) {
                    waitActivityDisplay = waitActivityDisplay(jadbDevice = jadbDevice, activityName = deeplinkWaitActivity)
                }
            }
        } else {
            //wait for start app
            delay(5000)

            if (!deeplinkWaitActivity.isNullOrBlank()) {
                waitActivityDisplay = waitActivityDisplay(jadbDevice = jadbDevice, activityName = deeplinkWaitActivity)
            }
        }

        return waitActivityDisplay
    }

    private suspend fun startDeeplinkWithActivity(
            jadbDevice: JadbDevice,
            deeplink: String,
            packageName: String,
            deeplinkStartActivity: String,
            extraDeeplinkKey: String,
            deeplinkWaitActivity: String?,
            waitStartActivityDisappear: String?
    ): Boolean {
        val builder = StringBuilder()
        val deeplinkOutStop = ByteArrayOutputStream()
        jadbDevice.executeShell(deeplinkOutStop, "am force-stop $packageName")
        builder.appendln(String(deeplinkOutStop.toByteArray()).trim())

        val deeplinkOut = ByteArrayOutputStream()
        jadbDevice.executeShell(deeplinkOut, "monkey -p $packageName -c android.intent.category.LAUNCHER 1")
        builder.appendln(String(deeplinkOut.toByteArray()).trim())

        delay(500)

        if (!waitStartActivityDisappear.isNullOrBlank()) {
            waitActivityDisappear(jadbDevice = jadbDevice, activityName = waitStartActivityDisappear)
        }

        goToDeviceHome(jadbDevice)

        val deeplinkOutDeeplink = ByteArrayOutputStream()
        jadbDevice.executeShell(deeplinkOutDeeplink, "am start -e \"$extraDeeplinkKey\" \"$deeplink\" -n \"$packageName/$deeplinkStartActivity\" ")
        builder.appendln(String(deeplinkOutDeeplink.toByteArray()).trim())

        val deeplinkOutMsg = builder.toString()
        logger.log("startDeeplink $deeplink $deeplinkOutMsg")
        var waitActivityDisplay = true

        if (!deeplinkWaitActivity.isNullOrBlank()) {
            waitActivityDisplay = waitActivityDisplay(jadbDevice = jadbDevice, activityName = deeplinkWaitActivity)
        } else {
            //wait for start app
            delay(5000)
        }

        return waitActivityDisplay
    }

    private fun fireEventTestCaseError(id: String, msg: String) {
        viewModelScope.launch(appDispatchers.main) {
            val indexTestCaseStep = processingSteps.indexOfFirst {
                it.id == id
            }

            val testCaseStep = processingSteps.getOrNull(indexTestCaseStep)
            if (testCaseStep != null) {
                val testCaseStepCopy = testCaseStep.copy(status = TestCaseStep.Status.ERROR(msg))
                processingSteps[indexTestCaseStep] = testCaseStepCopy
            }
        }
    }

    private fun fireEventTestCaseTimeout(id: String) {
        val indexTestCaseStep = processingSteps.indexOfFirst {
            it.id == id
        }

        val testCaseStep = processingSteps.getOrNull(indexTestCaseStep)
        if (testCaseStep != null) {
            val testCaseStepCopy = testCaseStep.copy(status = TestCaseStep.Status.TIMEOUT)
            processingSteps[indexTestCaseStep] = testCaseStepCopy
        }
    }

    fun requestInit() {
        requestFileConfigs()
        requestDevices()
    }

    private fun requestFileConfigs() {
        getFileConfigJob?.cancel()

        getFileConfigJob = viewModelScope.launch(appDispatchers.main) {
            val result: List<File> = withContext(appDispatchers.io) {
                getFileConfigs()
            }

            configFiles.clear()
            configFiles.addAll(result.filter { it.isFile }.map { file ->
                TestCaseConfigFile(file)
            })

            selectedConfigFile.value = configFiles.firstOrNull()
        }
    }

    private fun getFileConfigs(): List<File> {
        val folderConfigs = File(fileRoot, "configs")
        return folderConfigs.listFiles()?.toList().defaultEmpty()
    }

    private fun requestDevices() {
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
                        devices.addAll(it.map { device ->
                            TestCaseDevice(device)
                        })

                        selectedDevice.value = devices.firstOrNull()
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
    private fun takeScreenshot(jadbDevice: JadbDevice, externalStoragePath: String, fileScreenshot: File) {
        val startTimeMilis = System.currentTimeMillis()
        if (externalStoragePath.isNotBlank()) {
            val bout = ByteArrayOutputStream()
            val imagePathInDevice = "$externalStoragePath/screencap.png"
            jadbDevice.executeShell(bout, "screencap -p $imagePathInDevice")
            val resultScreenshot = String(bout.toByteArray())
            logger.log("resultScreenshot $resultScreenshot")

            jadbDevice.pull(RemoteFile(imagePathInDevice), fileScreenshot)
            logger.log("resultScreenshot save file $fileScreenshot")
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