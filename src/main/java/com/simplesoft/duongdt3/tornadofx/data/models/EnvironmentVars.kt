package com.simplesoft.duongdt3.tornadofx.data.models


data class EnvironmentVars(
        val vars: List<Var>
) {

    data class Var(
            val name: String,
            val value: String
    )

}