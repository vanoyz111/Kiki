package com.vano.kiki.scene

enum class MappingCategory(val label: String) {
    MOBA("Permainan MOBA"),
    SHOOTING("Permainan Menembak"),
    GAMEPAD("Gamepad (Joystick)"),
    UTILITY("Lainnya")
}

data class MappingActionType(
    val id: String,
    val label: String,
    val icon: String,
    val categories: List<MappingCategory>
)

object MappingActions {
    val all: List<MappingActionType> = listOf(
        MappingActionType("right_click_move", "Gerakan Tombol Kanan", "\uD83D\uDDB1\uFE0F", listOf(MappingCategory.MOBA)),
        MappingActionType("moba_skill", "Skill MOBA", "\u27A1\uFE0F", listOf(MappingCategory.MOBA)),
        MappingActionType("moba_skill_stick_r", "MOBA Skill Stick R", "\uD83C\uDFAF", listOf(MappingCategory.MOBA, MappingCategory.GAMEPAD)),
        MappingActionType("moba_skill_stick_l", "MOBA Skill Stick L", "\uD83C\uDFAF", listOf(MappingCategory.MOBA, MappingCategory.GAMEPAD)),
        MappingActionType("moba_cancel", "Batal MOBA", "\u274C", listOf(MappingCategory.MOBA)),
        MappingActionType("moba_dpad_skill", "Keterampilan MOBA (Dpad)", "\uD83C\uDF9E\uFE0F", listOf(MappingCategory.MOBA)),

        MappingActionType("dpad", "DPAD", "\uD83C\uDF9E\uFE0F", listOf(MappingCategory.GAMEPAD)),
        MappingActionType("move_stick_l", "Gerakan Stick L", "\uD83C\uDFAF", listOf(MappingCategory.GAMEPAD)),
        MappingActionType("camera_stick_r", "Kamera Stick R", "\uD83C\uDFAF", listOf(MappingCategory.SHOOTING, MappingCategory.GAMEPAD)),
        MappingActionType("free_look_stick_r", "Pandangan Bebas Stick R", "\uD83C\uDFAF", listOf(MappingCategory.SHOOTING, MappingCategory.GAMEPAD)),
        MappingActionType("recoil_control", "Kontrol Recoil (Gamepad)", "\u2B07\uFE0F", listOf(MappingCategory.SHOOTING, MappingCategory.GAMEPAD)),
        MappingActionType("mouse_virtual_stick_l", "Mouse Virtual Stick L", "\uD83D\uDDB1\uFE0F", listOf(MappingCategory.SHOOTING)),

        MappingActionType("aiming_mode", "Mode Aiming", "\uD83C\uDFAF", listOf(MappingCategory.SHOOTING)),
        MappingActionType("free_look", "Pandangan Bebas", "\uD83D\uDC41\uFE0F", listOf(MappingCategory.SHOOTING)),

        MappingActionType("open_backpack", "Buka Ransel (Tampilkan Mouse)", "\uD83C\uDF92", listOf(MappingCategory.UTILITY)),
        MappingActionType("close_backpack", "Tutup Ransel (Sembunyikan Mouse)", "\uD83C\uDF92", listOf(MappingCategory.UTILITY)),
        MappingActionType("backpack_advanced", "Ransel (Lanjutan)", "\uD83C\uDF92", listOf(MappingCategory.UTILITY)),
        MappingActionType("open_backpad_gamepad", "Buka Backpad (Gamepad)", "\uD83C\uDF9E\uFE0F", listOf(MappingCategory.UTILITY)),
        MappingActionType("release_mouse_temp", "Lepaskan Mouse Sementara", "\uD83D\uDDB1\uFE0F", listOf(MappingCategory.UTILITY)),
        MappingActionType("vehicle_toggle", "Naik/Turun Kendaraan (Lanjutan)", "\uD83D\uDE97", listOf(MappingCategory.UTILITY)),
        MappingActionType("long_press", "Tekan Lama", "\u23F1\uFE0F", listOf(MappingCategory.UTILITY)),
        MappingActionType("sequential_tap", "Ketuk Berurutan", "\uD83D\uDC46", listOf(MappingCategory.UTILITY))
    )

    fun byCategory(category: MappingCategory): List<MappingActionType> =
        all.filter { category in it.categories }
}
