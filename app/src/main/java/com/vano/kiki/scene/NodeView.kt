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

data class NodeViewHolder(val root: View, val gripView: View)

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

fun buildNodeView(context: Context, node: SceneNode, label: String): NodeViewHolder {
    val density = context.resources.displayMetrics.density
    val strokeWidth = (2 * density).toInt()
    val margin = (18 * density).toInt()

    val outer = FrameLayout(context).apply {
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.TRANSPARENT)
            setStroke(strokeWidth, 0xFFE8D44D.toInt(), 12f, 8f)
        }
        alpha = node.opacityPercent / 100f
    }

    val inner = FrameLayout(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ).apply { setMargins(margin, margin, margin, margin) }
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xE6000000.toInt())
            setStroke(strokeWidth, Color.WHITE)
        }
    }
    inner.addView(TextView(context).apply {
        text = label.take(10)
        setTextColor(Color.WHITE)
        textSize = 11f
        gravity = Gravity.CENTER
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
    })
    outer.addView(inner)

    val gripSize = (22 * density).toInt()
    val grip = View(context).apply {
        layoutParams = FrameLayout.LayoutParams(gripSize, gripSize, Gravity.BOTTOM or Gravity.END)
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xFFFF7FD1.toInt())
            setStroke((1 * density).toInt(), Color.WHITE)
        }
    }
    outer.addView(grip)

    return NodeViewHolder(outer, grip)
}
