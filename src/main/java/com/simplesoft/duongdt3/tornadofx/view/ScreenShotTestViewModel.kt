package com.simplesoft.duongdt3.tornadofx.view

import com.github.romankh3.image.comparison.model.Rectangle
import com.simplesoft.duongdt3.tornadofx.base.BaseViewModel
import com.simplesoft.duongdt3.tornadofx.data.CompareImageService
import com.simplesoft.duongdt3.tornadofx.data.FileOpener
import com.simplesoft.duongdt3.tornadofx.helper.AppDispatchers
import com.simplesoft.duongdt3.tornadofx.helper.formatyyyyMMddHHMM
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class ScreenShotTestViewModel(
        coroutineScope: CoroutineScope,
        appDispatchers: AppDispatchers,
        private val fileOpener: FileOpener,
        private val compareImageService: CompareImageService
) : BaseViewModel(coroutineScope, appDispatchers) {
    private var job: Job? = null
    val current1stFolder = SimpleObjectProperty<File>()
    val current2ndFolder = SimpleObjectProperty<File>()
    private val errorStatus = SimpleObjectProperty<ErrorStatus>()

    fun compareImages() {
        val folder1st: File? = current1stFolder.value
        val folder2nd: File? = current2ndFolder.value
        if (folder1st != null && folder1st.isDirectory && folder2nd != null && folder2nd.isDirectory) {
            job?.cancel()
            job = viewModelScope.launch(appDispatchers.main) {

                val resultDir = File("${Date().formatyyyyMMddHHMM()}_screenshot_result")
                val diffFolder = "images/diff"
                val oldFolder = "images/old"
                val newFolder = "images/new"

                val compareFolderResultJob: Deferred<CompareImageService.ReportCompareResult> = async(appDispatchers.io) {
                    compareImageService.compareFolderImages(
                            folder1st = folder1st,
                            folder2nd = folder2nd,
                            folderResult = resultDir,
                            prefixResultFile = "_compare_result",
                            excludedAreas = listOf(Rectangle(0, 0, 1080, 60), Rectangle(0, 1790, 1080, 1920)),
                            diffFolder = diffFolder,
                            oldFolder = oldFolder,
                            newFolder = newFolder
                    )
                }

                try {
                    val reportCompareResult = compareFolderResultJob.await()
                    val makeReportJob = async(appDispatchers.io) {

                        compareImageService.genReportHtml(
                                reportCompareResult = reportCompareResult,
                                dirReport = resultDir,
                                diffFolder = diffFolder,
                                oldFolder = oldFolder,
                                newFolder = newFolder
                        )

                    }

                    val reportFile = makeReportJob.await()
                    if (reportFile != null) {
                        fileOpener.openFile(reportFile)
                    } else {
                        errorStatus.value = ErrorStatus.ERROR
                    }
                } catch (e: Exception) {
                    errorStatus.value = ErrorStatus.ERROR
                }
            }
        } else {
            errorStatus.value = ErrorStatus.FOLDER_NOT_EXISTS
        }
    }


    fun update1stDir(choose1stDirectory: File) {
        current1stFolder.value = choose1stDirectory
    }

    fun update2ndDir(choose2ndDirectory: File) {
        current2ndFolder.value = choose2ndDirectory
    }


    enum class ErrorStatus {
        FOLDER_NOT_EXISTS,
        ERROR
    }
}