package com.simplesoft.duongdt3.tornadofx.view

import javafx.scene.Parent
import tornadofx.*

class ScreenshotTestFragment : Fragment() {
    private val screenShotTestView = ScreenShotTestView()
    override val root: Parent
        get() = screenShotTestView.root
}