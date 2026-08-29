package com.vano.kiki.scene

object NodeSizing {
    const val MIN_DP = 60
    const val MAX_DP = 280
    const val DEFAULT_DP = 140
}

data class SceneNode(
    val id: String,
    val actionId: String,
    var x: Int = 0,
    var y: Int = 400,
    var widthPx: Int = 0,
    var heightPx: Int = 0,
    val frequency: Int = 120,
    val movementStepMin: Int = 50,
    val movementStepMax: Int = 80,
    val deadZonePercent: Float = 15f,
    val flipX: Boolean = false,
    val flipY: Boolean = false,
    val opacityPercent: Int = 100,
    val description: String = ""
)
