package com.simplesoft.duongdt3.tornadofx.data

import java.io.BufferedReader
import java.io.InputStreamReader

class CmdExecutor {
    fun executeCommand(command: String?): String? {
        val output = StringBuilder()
        try {
            val p = Runtime.getRuntime().exec(command)
            p.waitFor()
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            var line: String?
            do {
                line = reader.readLine()
                if (line != null) {
                    output.append(line).append("\n")
                }
            } while (line != null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return output.toString()
    }
}