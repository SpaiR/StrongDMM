package strongdmm.ui.panel.preferences

import imgui.type.ImBoolean
import strongdmm.service.preferences.Preferences
import strongdmm.service.preferences.prefs.Preference
import strongdmm.service.preferences.prefs.enums.MapSaveMode
import strongdmm.service.preferences.prefs.enums.NudgeMode

class State {
    lateinit var providedPreferences: Preferences

    var isDoOpen: Boolean = false
    val isOpened: ImBoolean = ImBoolean(false)

    var checkOpenStatus: Boolean = false

    val mapSaveModes: List<MapSaveMode> = MapSaveMode.enums.map { it as MapSaveMode }
    val nudgeModes: List<NudgeMode> = NudgeMode.enums.map { it as NudgeMode }

    val preferencesByGroups: MutableMap<String, MutableList<Preference<Any>>> = mutableMapOf()
}
