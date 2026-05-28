package com.echo.app.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.echo.app.logging.EchoLog
import kotlinx.coroutines.CoroutineScope

class OverlayController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var root: FrameLayout? = null
    private var bubble: ImageButton? = null
    private var panel: LinearLayout? = null
    private var assistantText: TextView? = null
    private var input: EditText? = null
    private var thinking: TextView? = null

    private var onPushToTalk: (() -> Unit)? = null
    private var onSendText: ((String) -> Unit)? = null
    private var onStopEcho: (() -> Unit)? = null

    fun start(
        onPushToTalk: () -> Unit,
        onSendText: (String) -> Unit,
        onStopEcho: () -> Unit
    ) {
        this.onPushToTalk = onPushToTalk
        this.onSendText = onSendText
        this.onStopEcho = onStopEcho

        if (!Settings.canDrawOverlays(context)) {
            EchoLog.w("Overlay permission missing")
            return
        }

        if (root != null) return

        val container = FrameLayout(context)
        val b = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now)
            setBackgroundColor(0xCC111111.toInt())
            setOnClickListener { togglePanel() }
        }

        val p = createPanel().apply { isVisible = false }

        container.addView(b, FrameLayout.LayoutParams(dp(56), dp(56), Gravity.TOP or Gravity.START))
        container.addView(p, FrameLayout.LayoutParams(dp(320), ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START))

        attachDrag(b)
        root = container
        bubble = b
        panel = p

        wm.addView(container, overlayLayoutParams())
        EchoLog.i("Overlay started")
    }

    fun stop() {
        val r = root ?: return
        runCatching { wm.removeView(r) }
        root = null
        bubble = null
        panel = null
        assistantText = null
        input = null
        thinking = null
        EchoLog.i("Overlay stopped")
    }

    fun showAssistantText(text: String) {
        assistantText?.text = text
        panel?.isVisible = true
    }

    fun setAssistantThinking(isThinking: Boolean) {
        thinking?.isVisible = isThinking
    }

    private fun togglePanel() {
        panel?.isVisible = !(panel?.isVisible ?: false)
    }

    private fun createPanel(): LinearLayout {
        val wrapper = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xEE111111.toInt())
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val status = TextView(context).apply {
            setTextColor(0xFFEFEFEF.toInt())
            text = "ECHO"
        }

        val think = TextView(context).apply {
            setTextColor(0xFFB0B0B0.toInt())
            text = "Thinking..."
            isVisible = false
        }

        val assistant = TextView(context).apply {
            setTextColor(0xFFFFFFFF.toInt())
            text = ""
        }

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val edit = EditText(context).apply {
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFFAAAAAA.toInt())
            hint = "Type..."
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val send = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_menu_send)
            setBackgroundColor(0x00000000)
            setOnClickListener {
                val text = edit.text?.toString().orEmpty()
                edit.setText("")
                onSendText?.invoke(text)
            }
        }

        val mic = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now)
            setBackgroundColor(0x00000000)
            setOnClickListener { onPushToTalk?.invoke() }
        }

        val stop = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_delete)
            setBackgroundColor(0x00000000)
            setOnClickListener { onStopEcho?.invoke() }
        }

        row.addView(mic, LinearLayout.LayoutParams(dp(44), dp(44)))
        row.addView(edit)
        row.addView(send, LinearLayout.LayoutParams(dp(44), dp(44)))
        row.addView(stop, LinearLayout.LayoutParams(dp(44), dp(44)))

        wrapper.addView(status)
        wrapper.addView(think)
        wrapper.addView(assistant)
        wrapper.addView(row)

        assistantText = assistant
        input = edit
        thinking = think

        return wrapper
    }

    private fun overlayLayoutParams(): WindowManager.LayoutParams {
        val type =
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = dp(120)
        }
    }

    private fun attachDrag(view: View) {
        var downRawX = 0f
        var downRawY = 0f
        var downX = 0
        var downY = 0

        view.setOnTouchListener { _, event ->
            val params = (root?.layoutParams as? WindowManager.LayoutParams) ?: return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    downX = params.x
                    downY = params.y
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downRawX).toInt()
                    val dy = (event.rawY - downRawY).toInt()
                    params.x = downX + dx
                    params.y = downY + dy
                    runCatching { wm.updateViewLayout(root, params) }
                    true
                }
                else -> false
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
