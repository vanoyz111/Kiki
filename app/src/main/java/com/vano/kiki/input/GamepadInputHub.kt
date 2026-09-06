package com.vano.kiki.input

import android.util.Log
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell

private const val TAG = "GamepadInputHub"

object GamepadInputHub {

    private var running = false
    private val touchIgnored = setOf("BTN_TOUCH", "BTN_TOOL_FINGER", "BTN_TOOL_PEN", "BTN_STYLUS")

    private var captureCallback: ((String) -> Unit)? = null
    private val runtimeListeners = mutableListOf<(String) -> Unit>()

    fun start() {
        if (running) return
        running = true

        val output = object : CallbackList<String>() {
            override fun onAddElement(line: String) {
                try {
                    handleLine(line)
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal parse baris getevent: $line", e)
                }
            }
        }

        Shell.cmd("getevent -l").to(output).submit()
    }

    private fun handleLine(line: String) {
        val tokens = line.trim().split(Regex("\\s+"))
        val typeIndex = tokens.indexOfFirst { it == "EV_KEY" || it == "EV_ABS" }
        if (typeIndex == -1 || typeIndex + 2 >= tokens.size) return

        val type = tokens[typeIndex]
        val code = tokens[typeIndex + 1]
        val value = tokens[typeIndex + 2]

        val resolved: String? = when {
            type == "EV_KEY" && value.equals("DOWN", ignoreCase = true) ->
                if (code in touchIgnored) null else code
            type == "EV_ABS" && (code == "HAT0X" || code == "HAT0Y") -> {
                val v = value.toIntOrNull()
                if (v == null || v == 0) null else "${code}_${if (v > 0) "POS" else "NEG"}"
            }
            else -> null
        }
        if (resolved == null) return

        captureCallback?.let { cb -> captureCallback = null; cb(resolved) }
        runtimeListeners.toList().forEach {
            try { it(resolved) } catch (e: Exception) { Log.e(TAG, "Runtime listener error", e) }
        }
    }

    fun stop() {
        if (!running) return
        running = false
        captureCallback = null
        runtimeListeners.clear()
        Shell.cmd("pkill -f 'getevent -l'").submit()
    }

    fun captureNext(onKey: (String) -> Unit) {
        captureCallback = onKey
    }

    fun cancelCapture() {
        captureCallback = null
    }

    fun addRuntimeListener(listener: (String) -> Unit) {
        runtimeListeners.add(listener)
    }

    fun removeRuntimeListener(listener: (String) -> Unit) {
        runtimeListeners.remove(listener)
    }
}
