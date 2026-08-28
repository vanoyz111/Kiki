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
        var found: InputDevice? = null

        for (id in InputDevice.getDeviceIds()) {
            val device: InputDevice = inputManager.getInputDevice(id) ?: continue
            if (device.isVirtual) continue

            val name = device.name?.lowercase() ?: ""
            // HyperOS/MIUI daftarin virtual input node sendiri (mis. "uinput-xiaomi")
            // yang selalu ada walau gak ada gamepad fisik nyambung — wajib di-skip.
            if (name.contains("uinput")) continue

            val sources: Int = device.getSources()
            val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
            val isJoystick = (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK

            if (isGamepad || isJoystick) {
                found = device
                break
            }
        }

        _state.value = GamepadInfo(
            connected = found != null,
            deviceName = found?.getName()
        )
    }

    override fun onInputDeviceAdded(deviceId: Int) = refresh()
    override fun onInputDeviceRemoved(deviceId: Int) = refresh()
    override fun onInputDeviceChanged(deviceId: Int) = refresh()
}
