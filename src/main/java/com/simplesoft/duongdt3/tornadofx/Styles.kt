package com.simplesoft.duongdt3.tornadofx

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
    }

    init {
        label {
            fontSize = 14.px
            padding = box(4.px)
        }

        checkBox {
            padding = box(4.px)
            fontSize = 14.px
            fontWeight = FontWeight.MEDIUM
        }

        button {
            padding = box(4.px)
            fontSize = 14.px
            fontWeight = FontWeight.MEDIUM
        }

        Stylesheet.textArea {
            padding = box(4.px)
            fontSize = 14.px
            fontWeight = FontWeight.MEDIUM
            borderColor += box(Color.WHITE)
            backgroundColor += Color.WHITE
        }
    }
}