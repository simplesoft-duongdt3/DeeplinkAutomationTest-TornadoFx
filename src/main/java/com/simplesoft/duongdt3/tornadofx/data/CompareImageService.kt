package com.simplesoft.duongdt3.tornadofx.data

import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.ImageComparisonUtil
import com.github.romankh3.image.comparison.model.ImageComparisonState
import com.github.romankh3.image.comparison.model.Rectangle
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty
import net.lingala.zip4j.ZipFile
import java.io.*
import java.lang.StringBuilder


class CompareImageService {

    private val fileRoot = File(System.getProperty("user.dir"))

    fun compareFolderImages(
            folder1st: File,
            folder2nd: File,
            folderResult: File,
            prefixResultFile: String,
            excludedAreas: List<Rectangle>,
            diffFolder: String,
            oldFolder: String,
            newFolder: String
    ): ReportCompareResult {
        val imagesExts = listOf("jpg", "jpeg", "png")
        val listFiles1Source = folder1st.listFiles()?.toList().defaultEmpty().filter { imagesExts.contains(it.extension) }

        val listFiles2Source = folder2nd.listFiles()?.toList().defaultEmpty().filter { imagesExts.contains(it.extension) }

        if (listFiles1Source.isNotEmpty() && listFiles2Source.isNotEmpty()) {
            folderResult.deleteRecursively()
            folderResult.mkdirs()

            val dirDiffImagesResult = File(folderResult, diffFolder)
            dirDiffImagesResult.mkdirs()
            val dirOldImagesResult = File(folderResult, oldFolder)
            dirOldImagesResult.mkdirs()
            val dirNewImagesResult = File(folderResult, newFolder)
            dirNewImagesResult.mkdirs()

            listFiles1Source.forEach {
                it.copyTo(target = File(dirOldImagesResult, it.name), overwrite = true)
            }

            listFiles2Source.forEach {
                it.copyTo(target = File(dirNewImagesResult, it.name), overwrite = true)
            }

            val listFiles1 = dirOldImagesResult.listFiles()?.toList().defaultEmpty()
            val listFiles2 = dirNewImagesResult.listFiles()?.toList().defaultEmpty()
            val fileComparePairs: List<Pair<File?, File?>> = getFileComparePairs(listFiles1, listFiles2)
            val results = fileComparePairs.map { pair ->
                val file1 = pair.first
                val file2 = pair.second

                if (file1 != null && file2 != null) {
                    val fileDiff = File(dirDiffImagesResult, "${file1.nameWithoutExtension}$prefixResultFile.png")
                    val result: ImageComparisonResult = compareImages(file1, file2, fileDiff, excludedAreas)

                    //case match but still draw result file
                    if (result == ImageComparisonResult.MATCH) {
                        fileDiff.delete()
                    }

                    return@map ReportCompareResult.Item(
                            name = file1.name,
                            file1 = file1,
                            file2 = file2,
                            resultFile = if (fileDiff.exists()) {
                                fileDiff
                            } else {
                                null
                            },
                            isMatch = result == ImageComparisonResult.MATCH
                    )
                }

                return@map ReportCompareResult.Item(
                        name = when {
                            file1 != null -> {
                                file1.name
                            }
                            file2 != null -> {
                                file2.name
                            }
                            else -> {
                                ""
                            }
                        },
                        file1 = file1,
                        file2 = file2,
                        resultFile = null,
                        isMatch = false
                )
            }

            return ReportCompareResult(results)
        }

        throw Exception("Not found images.")
    }

    private fun getFileComparePairs(listFiles1: List<File>, listFiles2: List<File>): List<Pair<File?, File?>> {
        val pairs = mutableListOf<Pair<File?, File?>>()
        val mutableFiles1 = listFiles1.toMutableList()
        val mutableFiles2 = listFiles2.toMutableList()

        mutableFiles1.forEach { file1 ->
            val file2Match = mutableFiles2.firstOrNull { file2 ->
                file1.name == file2.name
            }

            if (file2Match != null) {
                mutableFiles2.remove(file2Match)
                pairs.add(Pair(file1, file2Match))
            } else {
                pairs.add(Pair(file1, null))
            }
        }
        mutableFiles2.forEach { file2 ->
            pairs.add(Pair(null, file2))
        }

        return pairs
    }

    fun compareImages(file1: File, file2: File, fileResult: File, excludedAreas: List<Rectangle>): ImageComparisonResult {
        val expectedImage = ImageComparisonUtil.readImageFromResources(file1.path)
        val actualImage = ImageComparisonUtil.readImageFromResources(file2.path)

        // where to save the result (leave null if you want to see the result in the UI)

        // where to save the result (leave null if you want to see the result in the UI)
        val resultDestination = File(fileResult.path)

        //Create ImageComparison object for it.
        val imageComparison = ImageComparison(expectedImage, actualImage, resultDestination)
        imageComparison.setExcludedAreas(excludedAreas)

        //Threshold - it's the max distance between non-equal pixels. By default it's 5.
        imageComparison.threshold = 5

        //RectangleListWidth - Width of the line that is drawn in the rectangle. By default it's 1.
        imageComparison.rectangleLineWidth = 2

        //DifferenceRectangleFilling - Fill the inside the difference rectangles with a transparent fill. By default it's false and 20.0% opacity.
        imageComparison.setDifferenceRectangleFilling(true, 30.0)

        //ExcludedRectangleFilling - Fill the inside the excluded rectangles with a transparent fill. By default it's false and 20.0% opacity.

        //ExcludedRectangleFilling - Fill the inside the excluded rectangles with a transparent fill. By default it's false and 20.0% opacity.
        imageComparison.setExcludedRectangleFilling(true, 30.0)
        imageComparison.isDrawExcludedRectangles = true

        //Destination. Before comparing also can be added destination file for result image.
        imageComparison.setDestination(resultDestination)

        //MinimalRectangleSize - The number of the minimal rectangle size. Count as (width x height).
        // by default it's 1.

        //MinimalRectangleSize - The number of the minimal rectangle size. Count as (width x height).
        // by default it's 1.
        imageComparison.minimalRectangleSize = 5

        //Change the level of the pixel tolerance:

        //Change the level of the pixel tolerance:
        imageComparison.pixelToleranceLevel = 0.1
        imageComparison.pixelToleranceLevel

        //After configuring the ImageComparison object, can be executed compare() method:
        val imageComparisonResult = imageComparison.compareImages()

        return when (imageComparisonResult.imageComparisonState) {
            ImageComparisonState.SIZE_MISMATCH -> ImageComparisonResult.SIZE_MISMATCH
            ImageComparisonState.MISMATCH -> ImageComparisonResult.IMAGES_MISMATCH
            ImageComparisonState.MATCH -> ImageComparisonResult.MATCH
            else -> ImageComparisonResult.IMAGES_MISMATCH
        }
    }


    class ReportCompareResult(val items: List<Item>) {
        class Item(val name: String, val file1: File?, val file2: File?, val resultFile: File?, val isMatch: Boolean)
    }


    fun genReportHtml(
            reportCompareResult: ReportCompareResult,
            dirReport: File,
            diffFolder: String,
            oldFolder: String,
            newFolder: String
    ): File? {
        val fileTemplateZip = File(fileRoot, "report_template/report_template.zip")
        ZipFile(fileTemplateZip).extractAll(dirReport.path);
        val templateFolder = File(dirReport, "template")
        val indexTemplateFile = File(templateFolder, "index.template")
        val successTemplateFile = File(templateFolder, "success.template")
        val failTemplateFile = File(templateFolder, "fail.template")

        val successTemplate = successTemplateFile.readText()
        val failTemplate = failTemplateFile.readText()
        var indexTemplate = indexTemplateFile.readText()

        val stringBuilder = StringBuilder()
        reportCompareResult.items.forEach { item ->
            var templateText = if (item.isMatch) {
                successTemplate
            } else {
                failTemplate
            }


            val oldImagePath = if (item.file1 != null) {
                oldFolder + "/" + item.file1.name
            } else {
                ""
            }

            val newImagePath = if (item.file2 != null) {
                newFolder + "/" + item.file2.name
            } else {
                ""
            }

            val diffImagePath = if (item.resultFile != null) {
                diffFolder + "/" + item.resultFile.name
            } else {
                ""
            }
            templateText = templateText.replace("{test_name}", item.name)
            templateText = templateText.replace("{old_image_path}", oldImagePath)
            templateText = templateText.replace("{new_image_path}", newImagePath)
            templateText = templateText.replace("{diff_image_path}", diffImagePath)
            stringBuilder.append(templateText)
        }

        indexTemplate = indexTemplate.replace("{testcase}", stringBuilder.toString())

        val fileIndexReport = File(dirReport, "index.html")
        fileIndexReport.writeText(indexTemplate)

        templateFolder.deleteRecursively()

        return fileIndexReport
    }


    enum class ImageComparisonResult {
        SIZE_MISMATCH,
        IMAGES_MISMATCH,
        MATCH
    }

}