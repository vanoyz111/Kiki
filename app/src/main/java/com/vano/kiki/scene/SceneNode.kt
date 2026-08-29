package com.vano.kiki.scene

data class SceneNode(
    val id: String,
    val actionId: String,
    var x: Int = 0,
    var y: Int = 400,
    val radiusPercent: Float = 18.5f,
    val frequency: Int = 120,
    val movementStepMin: Int = 50,
    val movementStepMax: Int = 80,
    val deadZonePercent: Float = 15f,
    val flipX: Boolean = false,
    val flipY: Boolean = false,
    val description: String = ""
)
