package com.simplesoft.duongdt3.tornadofx.view.models

import se.vidstige.jadb.JadbDevice

data class TestCaseDevice(val device: JadbDevice) {
    override fun toString(): String {
        return device.serial
    }
}
