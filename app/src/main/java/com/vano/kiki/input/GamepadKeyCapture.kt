package com.vano.kiki.input

import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell

class GamepadKeyCapture {

    private var listening = false

    fun startListening(onKeyDetected: (String) -> Unit) {
        if (listening) return
        listening = true

        val output = object : CallbackList<String>() {
            override fun onAddElement(line: String) {
                if (!listening) return
                val match = Regex("EV_KEY\\s+(KEY_\\w+|BTN_\\w+)\\s+DOWN").find(line)
                if (match != null) {
                    listening = false
                    onKeyDetected(match.groupValues[1])
                    Shell.cmd("pkill -f 'getevent -l'").submit()
                }
            }
        }

        Shell.cmd("getevent -l").to(output).submit()
    }

    fun stopListening() {
        if (!listening) return
        listening = false
        Shell.cmd("pkill -f 'getevent -l'").submit()
    }
}
