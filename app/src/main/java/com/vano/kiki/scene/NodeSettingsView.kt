package com.vano.kiki.scene

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.vano.kiki.input.GamepadKeyCapture

data class SettingsViewHolder(
    val root: View,
    val dragHandle: View,
    val gripView: View
)

fun buildNodeSettingsView(
    context: Context,
    node: SceneNode,
    title: String,
    onSave: (SceneNode) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
): SettingsViewHolder {
    val density = context.resources.displayMetrics.density
    val pad = (16 * density).toInt()

    var sizeDp = (node.widthPx / density).toInt().coerceIn(NodeSizing.MIN_DP, NodeSizing.MAX_DP)
    var opacity = node.opacityPercent
    var frequency = node.frequency
    var stepMin = node.movementStepMin
    var stepMax = node.movementStepMax
    var deadZone = node.deadZonePercent
    var flipX = node.flipX
    var flipY = node.flipY
    var pintasanKey = node.pintasanKey
    var captureRequestId = 0

    val capture = GamepadKeyCapture()
    fun cancelWrapped() { capture.stopListening(); onCancel() }
    fun deleteWrapped() { capture.stopListening(); onDelete() }

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

    val root = FrameLayout(context)

    val card = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(0xFF1C1C1EL.toInt())
    }

    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(pad, pad, pad + (28 * density).toInt(), pad / 2)
    }
    header.addView(TextView(context).apply {
        text = "\u2261  $title"
        setTextColor(Color.WHITE)
        textSize = 16f
        setTypeface(typeface, Typeface.BOLD)
    })
    card.addView(header)

    val scrollBody = ScrollView(context).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
    }
    val body = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(pad, 0, pad, pad)
    }

    body.addView(TextView(context).apply {
        text = "Tarik judul di atas buat pindah, tarik titik pink pojok buat resize."
        setTextColor(Color.GRAY)
        textSize = 11f
        setPadding(0, 0, 0, pad / 2)
    })

    val pintasanValueText = TextView(context).apply {
        text = pintasanKey ?: "(belum diatur)"
        setTextColor(Color.GRAY)
        textSize = 13f
    }
    val pintasanRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setOnClickListener {
            val requestId = ++captureRequestId
            pintasanValueText.text = "Menunggu input dari gamepad..."
            pintasanValueText.setTextColor(0xFFFF7FD1.toInt())

            capture.startListening { keyName ->
                if (requestId == captureRequestId) {
                    pintasanKey = keyName
                    pintasanValueText.text = keyName
                    pintasanValueText.setTextColor(Color.GRAY)
                }
            }

            pintasanValueText.postDelayed({
                if (requestId == captureRequestId && pintasanValueText.text == "Menunggu input dari gamepad...") {
                    capture.stopListening()
                    pintasanValueText.text = "Gak kedeteksi, tap lagi buat coba"
                    pintasanValueText.setTextColor(Color.GRAY)
                }
            }, 15000)
        }
    }
    pintasanRow.addView(TextView(context).apply {
        text = "Pintasan (tap lalu pencet tombol gamepad)"
        setTextColor(Color.WHITE)
        textSize = 14f
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    pintasanRow.addView(pintasanValueText)
    body.addView(pintasanRow)

    body.addView(sectionLabel("Ukuran tombol"))
    val sizeValue = valueLabel("$sizeDp dp")
    body.addView(sizeValue)
    body.addView(SeekBar(context).apply {
        max = NodeSizing.MAX_DP - NodeSizing.MIN_DP
        progress = sizeDp - NodeSizing.MIN_DP
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                sizeDp = value + NodeSizing.MIN_DP
                sizeValue.text = "$sizeDp dp"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    })

    body.addView(sectionLabel("Transparansi tombol (%)"))
    val opacityValue = valueLabel("$opacity")
    body.addView(opacityValue)
    body.addView(SeekBar(context).apply {
        max = 85
        progress = opacity - 15
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                opacity = value + 15
                opacityValue.text = "$opacity"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    })

    body.addView(sectionLabel("Frekuensi"))
    val freqValue = valueLabel("$frequency")
    body.addView(freqValue)
    body.addView(SeekBar(context).apply {
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

    body.addView(sectionLabel("Langkah Pergerakan (Min - Max)"))
    val stepValue = valueLabel("$stepMin - $stepMax")
    body.addView(stepValue)
    body.addView(SeekBar(context).apply {
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
    body.addView(SeekBar(context).apply {
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

    body.addView(sectionLabel("Daerah Mati (%)"))
    val deadZoneValue = valueLabel("${deadZone.toInt()}")
    body.addView(deadZoneValue)
    body.addView(SeekBar(context).apply {
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
    body.addView(flipXRow)

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
    body.addView(flipYRow)

    body.addView(sectionLabel("Deskripsi"))
    val descriptionInput = EditText(context).apply {
        setText(node.description)
        setTextColor(Color.WHITE)
        setHintTextColor(Color.GRAY)
        hint = "Opsional"
    }
    body.addView(descriptionInput)

    val actionsRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, pad, 0, 0)
    }
    actionsRow.addView(TextView(context).apply {
        text = "Hapus"
        setTextColor(0xFFFF5C5C.toInt())
        textSize = 14f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(pad, pad / 2, pad, pad / 2)
        setOnClickListener { deleteWrapped() }
    })
    actionsRow.addView(View(context).apply {
        layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
    })
    actionsRow.addView(Button(context).apply {
        text = "Batal"
        setOnClickListener { cancelWrapped() }
    })
    actionsRow.addView(Button(context).apply {
        text = "Simpan"
        setTextColor(Color.WHITE)
        setBackgroundColor(0xFFFF7FD1.toInt())
        setOnClickListener {
            capture.stopListening()
            onSave(
                node.copy(
                    widthPx = (sizeDp * density).toInt(),
                    heightPx = (sizeDp * density).toInt(),
                    opacityPercent = opacity,
                    frequency = frequency,
                    movementStepMin = stepMin,
                    movementStepMax = stepMax,
                    deadZonePercent = deadZone,
                    flipX = flipX,
                    flipY = flipY,
                    pintasanKey = pintasanKey,
                    description = descriptionInput.text.toString()
                )
            )
        }
    })
    body.addView(actionsRow)

    scrollBody.addView(body)
    card.addView(scrollBody)
    root.addView(card)

    val closeButton = TextView(context).apply {
        text = "\u2715"
        setTextColor(Color.WHITE)
        textSize = 18f
        setPadding(pad, pad, pad, pad / 2)
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END
        )
        setOnClickListener { cancelWrapped() }
    }
    root.addView(closeButton)

    val gripSize = (24 * density).toInt()
    val grip = View(context).apply {
        layoutParams = FrameLayout.LayoutParams(gripSize, gripSize, Gravity.BOTTOM or Gravity.END)
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xFFFF7FD1.toInt())
            setStroke((1 * density).toInt(), Color.WHITE)
        }
    }
    root.addView(grip)

    return SettingsViewHolder(root = root, dragHandle = header, gripView = grip)
}
