package com.simplesoft.duongdt3.tornadofx.data.models


import com.google.gson.annotations.SerializedName

data class EnvVarsConfigInput(
        @SerializedName("environmentVars")
        val environmentVars: List<Var?>?
) {
    data class Var(
            @SerializedName("key")
            val key: String?,
            @SerializedName("value")
            val value: String?
    )
}