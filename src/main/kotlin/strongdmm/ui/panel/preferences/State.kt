package strongdmm.ui.panel.preferences

import imgui.ImBool
import strongdmm.controller.preferences.MapSaveMode
import strongdmm.controller.preferences.NudgeMode
import strongdmm.controller.preferences.Preferences
import strongdmm.controller.preferences.Selectable

class State {
    lateinit var providedPreferences: Preferences

    var isDoOpen: Boolean = false
    val isOpened: ImBool = ImBool(false)

    var checkOpenStatus: Boolean = false

    val mapSaveModes: List<Selectable> = MapSaveMode.values().toList()
    val nudgeModes: List<NudgeMode> = NudgeMode.values().toList()
}
