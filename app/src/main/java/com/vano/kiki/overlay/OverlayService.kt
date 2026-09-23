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
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.app.ServiceCompat
import com.vano.kiki.MainActivity
import com.vano.kiki.engine.NativeTouchInjector
import com.vano.kiki.input.GamepadInputHub
import com.vano.kiki.scene.MappingActionType
import com.vano.kiki.scene.NodeSizing
import com.vano.kiki.scene.SceneNode
import com.vano.kiki.scene.buildGenerateMenuView
import com.vano.kiki.scene.buildNodeSettingsView
import com.vano.kiki.scene.buildNodeView
import com.vano.kiki.scene.makeDraggableOverlay
import java.util.UUID

private const val TAG = "KikiOverlay"

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager

    private var controlRoot: FrameLayout? = null
    private var controlParams: WindowManager.LayoutParams? = null

    private var menuView: View? = null
    private var layersView: View? = null
    private var quickSettingsView: View? = null
    private var settingsView: View? = null

    private val nodes = mutableMapOf<String, SceneNode>()
    private val nodeActions = mutableMapOf<String, MappingActionType>()
    private val nodeViews = mutableMapOf<String, View>()
    private val nodeParams = mutableMapOf<String, WindowManager.LayoutParams>()
    private var allNodesHidden = false

    private var nodeResizeStartW = 0
    private var nodeResizeStartH = 0
    private var nodeResizeStartRawX = 0f
    private var nodeResizeStartRawY = 0f

    private var settingsResizeStartW = 0
    private var settingsResizeStartH = 0
    private var settingsResizeStartRawX = 0f
    private var settingsResizeStartRawY = 0f
    private var rememberedSettingsWidth = 0
    private var rememberedSettingsHeight = 0

    private val tapSlopPx get() = 8 * resources.displayMetrics.density

    private val gamepadKeyListener: (String) -> Unit = { code -> handleGamepadKey(code) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            startForegroundWithNotification()
            showMainControl()
            NativeTouchInjector.start(
                this,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )
            GamepadInputHub.start()
            GamepadInputHub.addRuntimeListener(gamepadKeyListener)
        } catch (e: Exception) {
            Log.e(TAG, "onCreate gagal, service dihentikan", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        GamepadInputHub.removeRuntimeListener(gamepadKeyListener)
        GamepadInputHub.stop()
        NativeTouchInjector.stop()
        hideMenu(); hideLayers(); hideQuickSettings(); hideSettings()
        nodeViews.values.forEach { safeRemoveView(it) }
        nodeViews.clear()
        safeRemoveView(controlRoot)
        controlRoot = null
    }

    private fun handleGamepadKey(code: String) {
        try {
            nodes.values.forEach { node ->
                if (node.pintasanKey == code) {
                    val params = nodeParams[node.id] ?: return@forEach
                    val centerX = params.x + params.width / 2
                    val centerY = params.y + params.height / 2
                    NativeTouchInjector.tap(centerX, centerY)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleGamepadKey gagal", e)
        }
    }

    private fun safeAddView(view: View, params: WindowManager.LayoutParams) {
        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
            Log.e(TAG, "addView gagal", e)
        }
    }

    private fun safeRemoveView(view: View?) {
        if (view == null) return
        try {
            if (view.isAttachedToWindow) windowManager.removeView(view)
        } catch (e: Exception) {
            Log.e(TAG, "removeView gagal", e)
        }
    }

    private fun safeUpdateViewLayout(view: View, params: WindowManager.LayoutParams) {
        try {
            if (view.isAttachedToWindow) windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            Log.e(TAG, "updateViewLayout gagal", e)
        }
    }

    private fun startForegroundWithNotification() {
        val channelId = "kiki_overlay_channel"
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Kiki Overlay", NotificationManager.IMPORTANCE_LOW)
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

    private fun showMainControl() {
        val density = resources.displayMetrics.density
        val size = (56 * density).toInt()

        val root = FrameLayout(this)
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

        safeAddView(root, params)
        controlRoot = root
        controlParams = params
        renderCollapsed()
    }

    private fun renderCollapsed() {
        val root = controlRoot ?: return
        val params = controlParams ?: return
        val density = resources.displayMetrics.density

        root.removeAllViews()
        val bubble = buildCollapsedBubbleView(this)
        root.addView(bubble)

        params.width = (56 * density).toInt()
        params.height = (56 * density).toInt()
        safeUpdateViewLayout(root, params)

        bubble.makeDraggableOverlay(windowManager, params, tapSlopPx) { renderExpanded() }
    }

    private fun renderExpanded() {
        val root = controlRoot ?: return
        val params = controlParams ?: return

        root.removeAllViews()
        val toolbar = buildToolbarView(this)
        root.addView(toolbar.root)

        toolbar.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        params.width = toolbar.root.measuredWidth
        params.height = toolbar.root.measuredHeight
        safeUpdateViewLayout(root, params)

        toolbar.dragHandle.makeDraggableOverlay(windowManager, params, tapSlopPx) {}
        toolbar.collapseButton.setOnClickListener { renderCollapsed() }
        toolbar.addButton.setOnClickListener { showMenu() }
        toolbar.layersButton.setOnClickListener { showLayers() }
        toolbar.settingsButton.setOnClickListener { showQuickSettings() }
    }

    private fun showMenu() {
        if (menuView != null) { hideMenu(); return }
        hideLayers(); hideQuickSettings()
        val cp = controlParams ?: return

        val view = buildGenerateMenuView(
            context = this,
            onDismiss = { hideMenu() },
            onActionPicked = { action -> hideMenu(); placeNode(action, cp.x, cp.y) }
        )
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = cp.x
            y = cp.y + cp.height + (8 * resources.displayMetrics.density).toInt()
        }
        safeAddView(view, params)
        menuView = view
    }

    private fun hideMenu() {
        safeRemoveView(menuView)
        menuView = null
    }

    private fun showLayers() {
        if (layersView != null) { hideLayers(); return }
        hideMenu(); hideQuickSettings()
        val cp = controlParams ?: return
        renderLayersPanel(cp)
    }

    private fun renderLayersPanel(cp: WindowManager.LayoutParams) {
        safeRemoveView(layersView)
        val items = nodes.values.map { node ->
            Triple(node.id, nodeActions[node.id]?.label ?: node.actionId, nodeViews[node.id]?.visibility == View.GONE)
        }
        val view = buildLayersPanel(
            context = this,
            items = items,
            onToggleHidden = { id ->
                nodeViews[id]?.let { it.visibility = if (it.visibility == View.GONE) View.VISIBLE else View.GONE }
                renderLayersPanel(cp)
            },
            onDeleteItem = { id -> deleteNode(id); renderLayersPanel(cp) },
            onDismiss = { hideLayers() }
        )
        val params = WindowManager.LayoutParams(
            (260 * resources.displayMetrics.density).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = cp.x
            y = cp.y + cp.height + (8 * resources.displayMetrics.density).toInt()
        }
        safeAddView(view, params)
        layersView = view
    }

    private fun hideLayers() {
        safeRemoveView(layersView)
        layersView = null
    }

    private fun showQuickSettings() {
        if (quickSettingsView != null) { hideQuickSettings(); return }
        hideMenu(); hideLayers()
        val cp = controlParams ?: return

        val view = buildQuickSettingsPanel(
            context = this,
            allHidden = allNodesHidden,
            onToggleHideAll = { hide ->
                allNodesHidden = hide
                nodeViews.values.forEach { it.visibility = if (hide) View.GONE else View.VISIBLE }
            },
            onDismiss = { hideQuickSettings() }
        )
        val params = WindowManager.LayoutParams(
            (240 * resources.displayMetrics.density).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = cp.x
            y = cp.y + cp.height + (8 * resources.displayMetrics.density).toInt()
        }
        safeAddView(view, params)
        quickSettingsView = view
    }

    private fun hideQuickSettings() {
        safeRemoveView(quickSettingsView)
        quickSettingsView = null
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
        nodeActions[node.id] = action
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
                    nodeResizeStartW = params.width
                    nodeResizeStartH = params.height
                    nodeResizeStartRawX = event.rawX
                    nodeResizeStartRawY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - nodeResizeStartRawX).toInt()
                    val dy = (event.rawY - nodeResizeStartRawY).toInt()
                    params.width = (nodeResizeStartW + dx).coerceIn(minSize, maxSize)
                    params.height = (nodeResizeStartH + dy).coerceIn(minSize, maxSize)
                    safeUpdateViewLayout(holder.root, params)
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

        safeAddView(holder.root, params)
        nodeViews[node.id] = holder.root
        nodeParams[node.id] = params
    }

    private fun showNodeSettings(node: SceneNode, action: MappingActionType) {
        if (settingsView != null) hideSettings()

        val density = resources.displayMetrics.density
        val width = if (rememberedSettingsWidth > 0) rememberedSettingsWidth else (320 * density).toInt()
        val height = if (rememberedSettingsHeight > 0) rememberedSettingsHeight else (520 * density).toInt()

        val holder = buildNodeSettingsView(
            context = this,
            node = node,
            title = action.label,
            onSave = { updated -> hideSettings(); updateNode(updated, action) },
            onDelete = { hideSettings(); deleteNode(node.id) },
            onCancel = { hideSettings() }
        )

        val params = WindowManager.LayoutParams(
            width, height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = ((resources.displayMetrics.widthPixels - width) / 2).coerceAtLeast(0)
            y = ((resources.displayMetrics.heightPixels - height) / 3).coerceAtLeast(0)
        }

        holder.dragHandle.makeDraggableOverlay(windowManager, params, tapSlopPx) {}

        holder.gripView.setOnTouchListener { _, event ->
            val minW = (240 * density).toInt()
            val minH = (320 * density).toInt()
            val maxW = resources.displayMetrics.widthPixels
            val maxH = resources.displayMetrics.heightPixels
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    settingsResizeStartW = params.width
                    settingsResizeStartH = params.height
                    settingsResizeStartRawX = event.rawX
                    settingsResizeStartRawY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - settingsResizeStartRawX).toInt()
                    val dy = (event.rawY - settingsResizeStartRawY).toInt()
                    params.width = (settingsResizeStartW + dx).coerceIn(minW, maxW)
                    params.height = (settingsResizeStartH + dy).coerceIn(minH, maxH)
                    safeUpdateViewLayout(holder.root, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    rememberedSettingsWidth = params.width
                    rememberedSettingsHeight = params.height
                    true
                }
                else -> false
            }
        }

        safeAddView(holder.root, params)
        settingsView = holder.root
    }

    private fun hideSettings() {
        safeRemoveView(settingsView)
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
        nodeActions.remove(id)
    }

    private fun deleteNodeViewOnly(id: String) {
        safeRemoveView(nodeViews[id])
        nodeViews.remove(id)
        nodeParams.remove(id)
    }
}
