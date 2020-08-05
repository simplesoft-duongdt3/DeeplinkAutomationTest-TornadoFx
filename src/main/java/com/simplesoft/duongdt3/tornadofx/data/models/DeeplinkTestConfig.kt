package com.simplesoft.duongdt3.tornadofx.data.models

data class DeeplinkTestConfig(
        val deeplinks: List<Deeplink>,
        val packageName: String?,
        val waitStartActivityDisappear: String?
) {
    data class Deeplink(
            val activityName: String?,
            val deeplink: String
    )
}