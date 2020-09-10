package com.simplesoft.duongdt3.tornadofx.data

import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.ImageComparisonUtil
import com.github.romankh3.image.comparison.model.ImageComparisonState
import com.github.romankh3.image.comparison.model.Rectangle
import com.simplesoft.duongdt3.tornadofx.helper.defaultEmpty
import java.io.File


class CompareImageService {
    fun compareFolderImages(folder1st: File, folder2nd: File, dirResult: File, prefixResultFile: String, excludedAreas: List<Rectangle>): ReportCompareResult {
        val imagesExts = listOf("jpg", "jpeg", "png")
        val listFiles1 = folder1st.listFiles()?.toList().defaultEmpty()
        val folder1stImageExists = listFiles1.any { file ->
            imagesExts.contains(file.extension)
        }

        val listFiles2 = folder2nd.listFiles()?.toList().defaultEmpty()
        val folder2ndImageExists = listFiles2.any { file ->
            imagesExts.contains(file.extension)
        }

        if (folder1stImageExists && folder2ndImageExists) {
            dirResult.mkdirs()
            val fileComparePairs: List<Pair<File?, File?>> = getFileComparePairs(listFiles1, listFiles2)
            val results = fileComparePairs.map { pair ->
                val file1 = pair.first
                val file2 = pair.second

                if (file1 != null && file2 != null) {
                    val fileResult = File(dirResult, "${file1.name}$prefixResultFile.png")
                    val result: ImageComparisonResult = compareImages(file1, file2, fileResult, excludedAreas)

                    //case match but still draw result file
                    if(result == ImageComparisonResult.MATCH) {
                        fileResult.delete()
                    }

                    return@map ReportCompareResult.Item(
                            file1 = file1,
                            file2 = file2,
                            resultFile = if(fileResult.exists()) {
                                fileResult
                            } else {
                                null
                            },
                            isMatch = result == ImageComparisonResult.MATCH
                    )
                }

                return@map ReportCompareResult.Item(
                        file1 = file1,
                        file2 = file2,
                        resultFile = null,
                        isMatch = false
                )
            }

            return ReportCompareResult(results)
        }

        throw Exception("")
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


    class ReportCompareResult(items: List<Item>) {
        class Item(val file1: File?, val file2: File?, val resultFile: File?, val isMatch: Boolean)
    }


    fun genReportHtml(reportCompareResult: ReportCompareResult, fileReport: File) {
        //TODO gen report
    }

    enum class ImageComparisonResult {
        SIZE_MISMATCH,
        IMAGES_MISMATCH,
        MATCH
    }

}