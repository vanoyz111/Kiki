package com.vano.kiki.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.ServiceCompat
import com.vano.kiki.MainActivity
import com.vano.kiki.scene.buildGenerateMenuView
import kotlin.math.abs

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var menuView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        showBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        hideMenu()
        bubbleView?.let { windowManager.removeView(it) }
        bubbleView = null
    }

    private fun startForegroundWithNotification() {
        val channelId = "kiki_overlay_channel"
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Kiki Overlay", NotificationManager.IMPORTANCE_MIN)
        )
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Kiki aktif")
            .setContentText("Overlay sedang berjalan")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentIntent(openIntent)
            .build()
        ServiceCompat.startForeground(
            this, 1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    private fun showBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val bubble = TextView(this).apply {
            text = "K"
            setBackgroundColor(0xFFFF7FD1.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val size = (56 * resources.displayMetrics.density).toInt()
        val params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var downX = 0f
        var downY = 0f
        val tapSlop = 8 * resources.displayMetrics.density

        bubble.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    downX = event.rawX
                    downY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - touchX).toInt()
                    params.y = initialY + (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val moved = abs(event.rawX - downX) > tapSlop || abs(event.rawY - downY) > tapSlop
                    if (!moved) toggleMenu(params)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(bubble, params)
        bubbleView = bubble
    }

    private fun toggleMenu(bubbleParams: WindowManager.LayoutParams) {
        if (menuView != null) hideMenu() else showMenu(bubbleParams)
    }

    private fun showMenu(bubbleParams: WindowManager.LayoutParams) {
        val view = buildGenerateMenuView(
            context = this,
            onDismiss = { hideMenu() },
            onActionPicked = { hideMenu() /* TODO: taruh node ke Scene */ }
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bubbleParams.x
            y = bubbleParams.y + (64 * resources.displayMetrics.density).toInt()
        }

        windowManager.addView(view, params)
        menuView = view
    }

    private fun hideMenu() {
        menuView?.let { windowManager.removeView(it) }
        menuView = null
    }
}
