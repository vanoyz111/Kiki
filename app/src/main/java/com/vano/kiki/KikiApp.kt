package com.vano.kiki

import android.app.Application
import com.topjohnwu.superuser.Shell

class KikiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Shell.enableVerboseLogging = false
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }
}
