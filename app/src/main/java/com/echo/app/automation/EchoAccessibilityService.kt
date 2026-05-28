package com.echo.app.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.echo.app.logging.EchoLog

class EchoAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile
        var instance: EchoAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        instance = this
        EchoLog.i("Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    fun clickByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text).orEmpty()
        val target = nodes.firstOrNull { it.isClickable } ?: nodes.firstOrNull()
        return target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
    }

    fun typeFocused(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focus = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
        val args = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return focus.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    fun tap(x: Float, y: Float, onDone: (() -> Unit)? = null) {
        if (Build.VERSION.SDK_INT < 24) return
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onDone?.invoke()
            }
        }, null)
    }
}
