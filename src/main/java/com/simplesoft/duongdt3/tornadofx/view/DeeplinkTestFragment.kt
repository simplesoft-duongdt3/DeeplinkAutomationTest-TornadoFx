package com.simplesoft.duongdt3.tornadofx.view

import javafx.scene.Parent
import tornadofx.*

class DeeplinkTestFragment : Fragment() {
    private val deeplinkTestView = DeeplinkTestView()
    override val root: Parent
        get() = deeplinkTestView.root
}