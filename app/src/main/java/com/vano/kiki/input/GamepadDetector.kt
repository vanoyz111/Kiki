package com.vano.kiki.input

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GamepadInfo(
    val connected: Boolean = false,
    val deviceName: String? = null
)

class GamepadDetector(context: Context) : InputManager.InputDeviceListener {

    private val inputManager =
        context.getSystemService(Context.INPUT_SERVICE) as InputManager

    private val _state = MutableStateFlow(GamepadInfo())
    val state: StateFlow<GamepadInfo> = _state.asStateFlow()

    fun start() {
        inputManager.registerInputDeviceListener(this, null)
        refresh()
    }

    fun stop() {
        inputManager.unregisterInputDeviceListener(this)
    }

    private fun refresh() {
        val gamepad = InputDevice.getDeviceIds()
            .mapNotNull { InputDevice.getDevice(it) }
            .firstOrNull { device ->
                !device.isVirtual && (
                    (device.sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (device.sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                )
            }
        _state.value = GamepadInfo(
            connected = gamepad != null,
            deviceName = gamepad?.name
        )
    }

    override fun onInputDeviceAdded(deviceId: Int) = refresh()
    override fun onInputDeviceRemoved(deviceId: Int) = refresh()
    override fun onInputDeviceChanged(deviceId: Int) = refresh()
}
