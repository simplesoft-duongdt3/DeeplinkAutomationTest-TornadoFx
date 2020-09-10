package com.simplesoft.duongdt3.tornadofx.data.models


import com.google.gson.annotations.SerializedName

data class DeeplinkTestConfigInput(
        @SerializedName("deeplinks")
        val deeplinks: List<Deeplink?>?,
        @SerializedName("timeout_loading_milis")
        val timeoutLoadingMilis: Long?,
        @SerializedName("package_name")
        val packageName: String?,
        @SerializedName("deeplink_start_activity")
        val deeplinkStartActivity: String?,
        @SerializedName("extra_deeplink_key")
        val extraDeeplinkKey: String?,
        @SerializedName("wait_start_activity_disappear")
        val waitStartActivityDisappear: String?
) {
    data class Deeplink(
            @SerializedName("activity_name")
            val activityName: String?,
            @SerializedName("id")
            val id: String?,
            @SerializedName("deeplink")
            val deeplink: String?
    )
}