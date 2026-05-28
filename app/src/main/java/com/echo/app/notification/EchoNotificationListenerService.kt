package com.echo.app.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.echo.app.logging.EchoLog

class EchoNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val n = sbn ?: return
        if (n.packageName != "com.whatsapp") return

        val title = n.notification.extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = n.notification.extras.getCharSequence("android.text")?.toString().orEmpty()
        EchoLog.i("WhatsApp notif: title=$title text=$text")
    }
}
