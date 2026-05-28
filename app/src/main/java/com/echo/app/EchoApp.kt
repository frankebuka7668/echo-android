package com.echo.app

import android.app.Application
import com.echo.app.core.CrashRecovery
import com.echo.app.core.EchoServiceController
import com.echo.app.logging.EchoLog

class EchoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        EchoLog.init()
        CrashRecovery.install(this)
        EchoServiceController.ensureRunning(this)
    }
}
