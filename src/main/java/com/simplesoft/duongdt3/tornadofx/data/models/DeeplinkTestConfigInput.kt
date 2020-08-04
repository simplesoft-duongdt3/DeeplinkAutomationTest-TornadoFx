package com.simplesoft.duongdt3.tornadofx.data.models


import com.google.gson.annotations.SerializedName

data class DeeplinkTestConfigInput(
        @SerializedName("deeplinks")
        val deeplinks: List<Deeplink?>?,
        @SerializedName("package_name")
        val packageName: String?,
        @SerializedName("wait_start_activity_disappear")
        val waitStartActivityDisappear: String?
) {
    data class Deeplink(
            @SerializedName("activity_name")
            val activityName: String?,
            @SerializedName("deeplink")
            val deeplink: String?
    )
}