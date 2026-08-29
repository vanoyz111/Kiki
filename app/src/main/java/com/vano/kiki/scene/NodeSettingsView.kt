package com.vano.kiki.scene

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

fun buildNodeSettingsView(
    context: Context,
    node: SceneNode,
    title: String,
    onSave: (SceneNode) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
): View {
    val density = context.resources.displayMetrics.density
    val pad = (16 * density).toInt()

    var radius = node.radiusPercent
    var frequency = node.frequency
    var stepMin = node.movementStepMin
    var stepMax = node.movementStepMax
    var deadZone = node.deadZonePercent
    var flipX = node.flipX
    var flipY = node.flipY

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(pad, pad, pad, pad)
    }

    fun sectionLabel(text: String) = TextView(context).apply {
        this.text = text
        setTextColor(Color.WHITE)
        setTypeface(typeface, Typeface.BOLD)
        textSize = 14f
        setPadding(0, pad, 0, 4)
    }

    fun valueLabel(text: String) = TextView(context).apply {
        this.text = text
        setTextColor(Color.LTGRAY)
        textSize = 12f
    }

    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    header.addView(TextView(context).apply {
        text = title
        setTextColor(Color.WHITE)
        textSize = 17f
        setTypeface(typeface, Typeface.BOLD)
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    header.addView(TextView(context).apply {
        text = "\u2715"
        setTextColor(Color.WHITE)
        textSize = 18f
        setOnClickListener { onCancel() }
    })
    container.addView(header)

    val pintasanRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, pad, 0, 0)
    }
    pintasanRow.addView(TextView(context).apply {
        text = "Pintasan"
        setTextColor(Color.WHITE)
        textSize = 14f
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    pintasanRow.addView(TextView(context).apply {
        text = "(belum diatur)"
        setTextColor(Color.GRAY)
        textSize = 13f
    })
    container.addView(pintasanRow)

    container.addView(sectionLabel("Nilai radius (%)"))
    val radiusValue = valueLabel("${radius.toInt()}")
    container.addView(radiusValue)
    container.addView(SeekBar(context).apply {
        max = 100
        progress = radius.toInt()
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                radius = value.toFloat(); radiusValue.text = "$value"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    })

    container.addView(sectionLabel("Frekuensi"))
    val freqValue = valueLabel("$frequency")
    container.addView(freqValue)
    container.addView(SeekBar(context).apply {
        max = 240
        progress = frequency
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                frequency = value; freqValue.text = "$value"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    })

    container.addView(sectionLabel("Langkah Pergerakan (Min - Max)"))
    val stepValue = valueLabel("$stepMin - $stepMax")
    container.addView(stepValue)
    container.addView(SeekBar(context).apply {
        max = 200
        progress = stepMin
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                stepMin = value; stepValue.text = "$stepMin - $stepMax"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    })
    container.addView(SeekBar(context).apply {
        max = 200
        progress = stepMax
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                stepMax = value; stepValue.text = "$stepMin - $stepMax"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    })

    container.addView(sectionLabel("Daerah Mati (%)"))
    val deadZoneValue = valueLabel("${deadZone.toInt()}")
    container.addView(deadZoneValue)
    container.addView(SeekBar(context).apply {
        max = 100
        progress = deadZone.toInt()
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                deadZone = value.toFloat(); deadZoneValue.text = "$value"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    })

    val flipXRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, pad, 0, 0)
    }
    flipXRow.addView(TextView(context).apply {
        text = "Balik X"; setTextColor(Color.WHITE); textSize = 14f
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    flipXRow.addView(Switch(context).apply {
        isChecked = flipX
        setOnCheckedChangeListener { _, checked -> flipX = checked }
    })
    container.addView(flipXRow)

    val flipYRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, pad / 2, 0, 0)
    }
    flipYRow.addView(TextView(context).apply {
        text = "Balik Y"; setTextColor(Color.WHITE); textSize = 14f
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    flipYRow.addView(Switch(context).apply {
        isChecked = flipY
        setOnCheckedChangeListener { _, checked -> flipY = checked }
    })
    container.addView(flipYRow)

    container.addView(sectionLabel("Deskripsi"))
    val descriptionInput = EditText(context).apply {
        setText(node.description)
        setTextColor(Color.WHITE)
        setHintTextColor(Color.GRAY)
        hint = "Opsional"
    }
    container.addView(descriptionInput)

    val actions = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, pad, 0, 0)
    }
    actions.addView(TextView(context).apply {
        text = "Hapus"
        setTextColor(0xFFFF5C5C.toInt())
        textSize = 14f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(pad, pad / 2, pad, pad / 2)
        setOnClickListener { onDelete() }
    })
    actions.addView(View(context).apply {
        layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
    })
    actions.addView(Button(context).apply {
        text = "Batal"
        setOnClickListener { onCancel() }
    })
    actions.addView(Button(context).apply {
        text = "Simpan"
        setTextColor(Color.WHITE)
        setBackgroundColor(0xFFFF7FD1.toInt())
        setOnClickListener {
            onSave(
                node.copy(
                    radiusPercent = radius,
                    frequency = frequency,
                    movementStepMin = stepMin,
                    movementStepMax = stepMax,
                    deadZonePercent = deadZone,
                    flipX = flipX,
                    flipY = flipY,
                    description = descriptionInput.text.toString()
                )
            )
        }
    })
    container.addView(actions)

    return ScrollView(context).apply {
        layoutParams = ViewGroup.LayoutParams((320 * density).toInt(), (520 * density).toInt())
        setBackgroundColor(0xFF1C1C1EL.toInt())
        addView(container)
    }
}
