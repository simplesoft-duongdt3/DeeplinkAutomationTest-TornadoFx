package com.simplesoft.duongdt3.tornadofx.data.models

data class DeeplinkTestConfig(
        val deeplinks: List<Deeplink>,
        val timeoutLoadingMilis: Long,
        val packageName: String?,
        val waitStartActivityDisappear: String?,
        val deeplinkStartActivity: String?,
        val extraDeeplinkKey: String?,
        val mockServerUrl: String?
) {
    data class Deeplink(
            val id: String,
            val activityName: String?,
            val ignoreWaitStartActivity: Boolean,
            val mockServerRules: List<Rule>,
            val deeplink: String
    )

    data class Rule(
            val request: RequestConfig,
            val response: ResponseConfig
    ) {
        data class RequestConfig(
                val method: String,
                val path: String,
                val body: String
        )

        data class ResponseConfig(
                val statusCode: Int,
                val body: String,
                val delayMillis: Long
        )
    }
}