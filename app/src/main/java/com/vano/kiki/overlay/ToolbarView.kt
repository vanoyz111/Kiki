package com.vano.kiki.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

data class ToolbarViewHolder(
    val root: View,
    val dragHandle: View,
    val collapseButton: View,
    val layersButton: View,
    val addButton: View,
    val settingsButton: View
)

fun buildCollapsedBubbleView(context: Context): View {
    return TextView(context).apply {
        text = "K"
        setBackgroundColor(0xFFFF7FD1.toInt())
        setTextColor(Color.WHITE)
        textSize = 18f
        gravity = Gravity.CENTER
    }
}

fun buildToolbarView(context: Context): ToolbarViewHolder {
    val density = context.resources.displayMetrics.density
    val padH = (12 * density).toInt()
    val padV = (10 * density).toInt()

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(padH, padV, padH, padV)
        setBackgroundColor(0xFF1C1C1EL.toInt())
    }

    fun icon(text: String) = TextView(context).apply {
        this.text = text
        setTextColor(Color.WHITE)
        textSize = 18f
        setPadding(padH, 0, padH, 0)
    }

    val dragHandle = TextView(context).apply {
        text = "kiki"
        setTextColor(Color.WHITE)
        textSize = 15f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, 0, padH, 0)
    }
    val collapse = icon("\u2039")
    val layers = icon("\uD83D\uDCD1")
    val add = icon("\uFF0B")
    val settings = icon("\u2699")

    root.addView(dragHandle)
    root.addView(collapse)
    root.addView(layers)
    root.addView(add)
    root.addView(settings)

    return ToolbarViewHolder(root, dragHandle, collapse, layers, add, settings)
}

fun buildLayersPanel(
    context: Context,
    items: List<Triple<String, String, Boolean>>,
    onToggleHidden: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onDismiss: () -> Unit
): View {
    val density = context.resources.displayMetrics.density
    val pad = (14 * density).toInt()

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(pad, pad, pad, pad)
    }

    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    header.addView(TextView(context).apply {
        text = "Layer tombol"
        setTextColor(Color.WHITE)
        setTypeface(typeface, Typeface.BOLD)
        textSize = 15f
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    header.addView(TextView(context).apply {
        text = "\u2715"
        setTextColor(Color.WHITE)
        setOnClickListener { onDismiss() }
    })
    container.addView(header)

    if (items.isEmpty()) {
        container.addView(TextView(context).apply {
            text = "Belum ada tombol. Tap + buat nambah."
            setTextColor(Color.GRAY)
            textSize = 12f
            setPadding(0, pad, 0, 0)
        })
    }

    items.forEach { (id, label, hidden) ->
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, pad / 2, 0, pad / 2)
        }
        row.addView(TextView(context).apply {
            text = label
            setTextColor(if (hidden) Color.GRAY else Color.WHITE)
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        row.addView(TextView(context).apply {
            text = if (hidden) "Tampilkan" else "Sembunyikan"
            setTextColor(0xFF6FC3FF.toInt())
            textSize = 12f
            setPadding(pad, 0, pad, 0)
            setOnClickListener { onToggleHidden(id) }
        })
        row.addView(TextView(context).apply {
            text = "Hapus"
            setTextColor(0xFFFF5C5C.toInt())
            textSize = 12f
            setOnClickListener { onDeleteItem(id) }
        })
        container.addView(row)
    }

    return ScrollView(context).apply {
        setBackgroundColor(0xFF1C1C1EL.toInt())
        addView(container)
    }
}

fun buildQuickSettingsPanel(
    context: Context,
    allHidden: Boolean,
    onToggleHideAll: (Boolean) -> Unit,
    onDismiss: () -> Unit
): View {
    val density = context.resources.displayMetrics.density
    val pad = (14 * density).toInt()

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(pad, pad, pad, pad)
        setBackgroundColor(0xFF1C1C1EL.toInt())
    }

    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    header.addView(TextView(context).apply {
        text = "Pengaturan"
        setTextColor(Color.WHITE)
        setTypeface(typeface, Typeface.BOLD)
        textSize = 15f
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    header.addView(TextView(context).apply {
        text = "\u2715"
        setTextColor(Color.WHITE)
        setOnClickListener { onDismiss() }
    })
    container.addView(header)

    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, pad, 0, 0)
    }
    row.addView(TextView(context).apply {
        text = "Sembunyikan semua tombol"
        setTextColor(Color.WHITE)
        textSize = 13f
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    row.addView(Switch(context).apply {
        isChecked = allHidden
        setOnCheckedChangeListener { _, checked -> onToggleHideAll(checked) }
    })
    container.addView(row)

    container.addView(TextView(context).apply {
        text = "Mode Hibrida, sensitivitas mouse, dan polling rate nunggu engine input siap."
        setTextColor(Color.GRAY)
        textSize = 11f
        setPadding(0, pad, 0, 0)
    })

    return container
}
