package com.vano.kiki.engine

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "NativeTouchInjector"
private const val PIPE_PATH = "/data/local/tmp/keymapper_pipe"

object NativeTouchInjector {

    private var pipeStream: FileOutputStream? = null
    private var daemonStarted = false

    fun start(context: Context, screenWidth: Int, screenHeight: Int) {
        if (daemonStarted) return
        daemonStarted = true

        try {
            val daemonPath = "${context.applicationInfo.nativeLibraryDir}/libkikidaemon.so"
            Shell.cmd(
                "pkill -f libkikidaemon.so",
                "chmod 755 $daemonPath",
                "nohup $daemonPath $screenWidth $screenHeight > /data/local/tmp/kikidaemon.log 2>&1 &"
            ).submit { result ->
                Log.i(TAG, "Launch daemon: out=${result.out} err=${result.err}")
                openPipeAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal start daemon", e)
            daemonStarted = false
        }
    }

    private fun openPipeAsync() {
        Thread {
            try {
                Thread.sleep(400)
                pipeStream = FileOutputStream(File(PIPE_PATH))
                Log.i(TAG, "Pipe ke daemon kebuka")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal buka pipe ke daemon", e)
            }
        }.start()
    }

    fun tap(x: Int, y: Int) {
        val stream = pipeStream
        if (stream == null) {
            Log.e(TAG, "Pipe belum siap, tap $x,$y dilewati")
            return
        }
        try {
            stream.write("TAP $x $y\n".toByteArray())
            stream.flush()
        } catch (e: IOException) {
            Log.e(TAG, "Gagal tulis ke pipe, coba buka ulang", e)
            pipeStream = null
            openPipeAsync()
        }
    }

    fun stop() {
        try {
            pipeStream?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Gagal tutup pipe", e)
        }
        pipeStream = null
        Shell.cmd("pkill -f libkikidaemon.so").submit()
        daemonStarted = false
    }
}
