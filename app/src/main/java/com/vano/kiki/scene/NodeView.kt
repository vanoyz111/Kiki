package com.vano.kiki.scene

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.abs

fun View.makeDraggableOverlay(
    windowManager: WindowManager,
    params: WindowManager.LayoutParams,
    tapSlopPx: Float,
    onTap: () -> Unit
) {
    var initialX = 0
    var initialY = 0
    var touchX = 0f
    var touchY = 0f
    var downX = 0f
    var downY = 0f

    setOnTouchListener { view, event ->
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
                val moved = abs(event.rawX - downX) > tapSlopPx || abs(event.rawY - downY) > tapSlopPx
                if (!moved) onTap()
                true
            }
            else -> false
        }
    }
}

fun buildNodeView(context: Context, node: SceneNode, label: String): View {
    val density = context.resources.displayMetrics.density
    val outerSize = (140 * density * (node.radiusPercent / 18.5f)).toInt()
        .coerceIn((80 * density).toInt(), (260 * density).toInt())
    val innerSize = (70 * density).toInt()

    val outer = FrameLayout(context).apply {
        layoutParams = FrameLayout.LayoutParams(outerSize, outerSize)
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.TRANSPARENT)
            setStroke((2 * density).toInt(), 0xFFE8D44D.toInt(), 12f, 8f)
        }
    }

    val inner = FrameLayout(context).apply {
        layoutParams = FrameLayout.LayoutParams(innerSize, innerSize, Gravity.CENTER)
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xE6000000.toInt())
            setStroke((2 * density).toInt(), Color.WHITE)
        }
    }

    inner.addView(TextView(context).apply {
        text = label.take(8)
        setTextColor(Color.WHITE)
        textSize = 11f
        gravity = Gravity.CENTER
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
    })

    outer.addView(inner)
    return outer
}
