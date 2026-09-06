package com.vano.kiki.input

import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell

class GamepadKeyCapture {

    private var listening = false
    private var startTimeMs = 0L

    private val ignoredCodes = setOf("BTN_TOUCH", "BTN_TOOL_FINGER", "BTN_TOOL_PEN", "BTN_STYLUS")

    fun startListening(onKeyDetected: (String) -> Unit) {
        if (listening) return
        listening = true
        startTimeMs = System.currentTimeMillis()

        val output = object : CallbackList<String>() {
            override fun onAddElement(line: String) {
                if (!listening) return
                if (System.currentTimeMillis() - startTimeMs < 400) return

                val tokens = line.trim().split(Regex("\\s+"))
                val typeIndex = tokens.indexOfFirst { it == "EV_KEY" || it == "EV_ABS" }
                if (typeIndex == -1 || typeIndex + 2 >= tokens.size) return

                val type = tokens[typeIndex]
                val code = tokens[typeIndex + 1]
                val value = tokens[typeIndex + 2]

                when {
                    type == "EV_KEY" && value.equals("DOWN", ignoreCase = true) -> {
                        if (code in ignoredCodes) return
                        finish(code, onKeyDetected)
                    }
                    type == "EV_ABS" && (code == "HAT0X" || code == "HAT0Y") -> {
                        val v = value.toIntOrNull() ?: return
                        if (v == 0) return
                        finish("${code}_${if (v > 0) "POS" else "NEG"}", onKeyDetected)
                    }
                }
            }
        }

        Shell.cmd("getevent -l").to(output).submit()
    }

    private fun finish(code: String, onKeyDetected: (String) -> Unit) {
        listening = false
        onKeyDetected(code)
        Shell.cmd("pkill -f 'getevent -l'").submit()
    }

    fun stopListening() {
        if (!listening) return
        listening = false
        Shell.cmd("pkill -f 'getevent -l'").submit()
    }
}
