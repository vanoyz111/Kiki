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
import com.vano.kiki.scene.MappingActionType
import com.vano.kiki.scene.NodeSizing
import com.vano.kiki.scene.SceneNode
import com.vano.kiki.scene.buildGenerateMenuView
import com.vano.kiki.scene.buildNodeSettingsView
import com.vano.kiki.scene.buildNodeView
import com.vano.kiki.scene.makeDraggableOverlay
import java.util.UUID

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var menuView: View? = null
    private var settingsView: View? = null

    private val nodes = mutableMapOf<String, SceneNode>()
    private val nodeViews = mutableMapOf<String, View>()
    private val nodeParams = mutableMapOf<String, WindowManager.LayoutParams>()

    private var resizeStartWidth = 0
    private var resizeStartHeight = 0
    private var resizeStartRawX = 0f
    private var resizeStartRawY = 0f

    private val tapSlopPx get() = 8 * resources.displayMetrics.density

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
        hideSettings()
        nodeViews.values.forEach { windowManager.removeView(it) }
        nodeViews.clear()
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

        bubble.makeDraggableOverlay(windowManager, params, tapSlopPx) { toggleMenu() }

        windowManager.addView(bubble, params)
        bubbleView = bubble
        bubbleParams = params
    }

    private fun toggleMenu() {
        if (menuView != null) hideMenu() else showMenu()
    }

    private fun showMenu() {
        val bp = bubbleParams ?: return
        val view = buildGenerateMenuView(
            context = this,
            onDismiss = { hideMenu() },
            onActionPicked = { action ->
                hideMenu()
                placeNode(action, bp.x, bp.y)
            }
        )
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bp.x
            y = bp.y + (64 * resources.displayMetrics.density).toInt()
        }
        windowManager.addView(view, params)
        menuView = view
    }

    private fun hideMenu() {
        menuView?.let { windowManager.removeView(it) }
        menuView = null
    }

    private fun placeNode(action: MappingActionType, nearX: Int, nearY: Int) {
        val density = resources.displayMetrics.density
        val defaultSize = (NodeSizing.DEFAULT_DP * density).toInt()
        val node = SceneNode(
            id = UUID.randomUUID().toString(),
            actionId = action.id,
            x = nearX,
            y = nearY + (120 * density).toInt(),
            widthPx = defaultSize,
            heightPx = defaultSize
        )
        renderNode(node, action)
    }

    private fun renderNode(node: SceneNode, action: MappingActionType) {
        nodes[node.id] = node
        val holder = buildNodeView(this, node, action.label)

        val params = WindowManager.LayoutParams(
            node.widthPx, node.heightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = node.x
            y = node.y
        }

        holder.root.makeDraggableOverlay(windowManager, params, tapSlopPx) {
            showNodeSettings(node, action)
        }

        holder.gripView.setOnTouchListener { _, event ->
            val density = resources.displayMetrics.density
            val minSize = (NodeSizing.MIN_DP * density).toInt()
            val maxSize = (NodeSizing.MAX_DP * density).toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    resizeStartWidth = params.width
                    resizeStartHeight = params.height
                    resizeStartRawX = event.rawX
                    resizeStartRawY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - resizeStartRawX).toInt()
                    val dy = (event.rawY - resizeStartRawY).toInt()
                    params.width = (resizeStartWidth + dx).coerceIn(minSize, maxSize)
                    params.height = (resizeStartHeight + dy).coerceIn(minSize, maxSize)
                    windowManager.updateViewLayout(holder.root, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    node.widthPx = params.width
                    node.heightPx = params.height
                    true
                }
                else -> false
            }
        }

        windowManager.addView(holder.root, params)
        nodeViews[node.id] = holder.root
        nodeParams[node.id] = params
    }

    private fun showNodeSettings(node: SceneNode, action: MappingActionType) {
        if (settingsView != null) hideSettings()

        val view = buildNodeSettingsView(
            context = this,
            node = node,
            title = action.label,
            onSave = { updated -> hideSettings(); updateNode(updated, action) },
            onDelete = { hideSettings(); deleteNode(node.id) },
            onCancel = { hideSettings() }
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        windowManager.addView(view, params)
        settingsView = view
    }

    private fun hideSettings() {
        settingsView?.let { windowManager.removeView(it) }
        settingsView = null
    }

    private fun updateNode(updated: SceneNode, action: MappingActionType) {
        val oldParams = nodeParams[updated.id]
        val keepX = oldParams?.x ?: updated.x
        val keepY = oldParams?.y ?: updated.y
        deleteNodeViewOnly(updated.id)
        renderNode(updated.copy(x = keepX, y = keepY), action)
    }

    private fun deleteNode(id: String) {
        deleteNodeViewOnly(id)
        nodes.remove(id)
    }

    private fun deleteNodeViewOnly(id: String) {
        nodeViews[id]?.let { windowManager.removeView(it) }
        nodeViews.remove(id)
        nodeParams.remove(id)
    }
}
