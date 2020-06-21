package strongdmm.ui.panel.preferences

import imgui.ImBool
import strongdmm.service.preferences.model.MapSaveMode
import strongdmm.service.preferences.model.NudgeMode
import strongdmm.service.preferences.Preferences
import strongdmm.service.preferences.model.Selectable

class State {
    lateinit var providedPreferences: Preferences

    var isDoOpen: Boolean = false
    val isOpened: ImBool = ImBool(false)

    var checkOpenStatus: Boolean = false

    val mapSaveModes: List<Selectable> = MapSaveMode.values().toList()
    val nudgeModes: List<NudgeMode> = NudgeMode.values().toList()
}
