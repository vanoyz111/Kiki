package com.vano.kiki.scene

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

fun buildGenerateMenuView(
    context: Context,
    onDismiss: () -> Unit,
    onActionPicked: (MappingActionType) -> Unit
): View {
    val density = context.resources.displayMetrics.density
    val pad = (16 * density).toInt()

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(pad, pad, pad, pad)
    }

    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    header.addView(TextView(context).apply {
        text = "Hasilkan"
        setTextColor(Color.WHITE)
        textSize = 16f
        setTypeface(typeface, Typeface.BOLD)
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    header.addView(TextView(context).apply {
        text = "\u2715"
        setTextColor(Color.WHITE)
        textSize = 18f
        setPadding(pad, 0, 0, 0)
        setOnClickListener { onDismiss() }
    })
    container.addView(header)

    MappingCategory.entries.forEach { category ->
        val actions = MappingActions.byCategory(category)
        if (actions.isNotEmpty()) {
            container.addView(TextView(context).apply {
                text = category.label
                setTextColor(Color.GRAY)
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, pad, 0, pad / 2)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            })

            actions.forEach { action ->
                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, pad / 2, 0, pad / 2)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener { onActionPicked(action) }
                }
                row.addView(TextView(context).apply {
                    text = action.icon
                    textSize = 18f
                    setPadding(0, 0, pad, 0)
                })
                row.addView(TextView(context).apply {
                    text = action.label
                    setTextColor(Color.WHITE)
                    textSize = 15f
                    setTypeface(typeface, Typeface.BOLD)
                })
                container.addView(row)
            }
        }
    }

    return ScrollView(context).apply {
        layoutParams = ViewGroup.LayoutParams((300 * density).toInt(), (440 * density).toInt())
        setBackgroundColor(0xFF1C1C1EL.toInt())
        addView(container)
    }
}
