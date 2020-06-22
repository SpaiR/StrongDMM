package strongdmm.ui.panel.preferences

import imgui.ImBool
import strongdmm.service.preferences.prefs.Preference
import strongdmm.service.preferences.prefs.enums.MapSaveMode
import strongdmm.service.preferences.prefs.enums.NudgeMode
import strongdmm.service.preferences.Preferences

class State {
    lateinit var providedPreferences: Preferences

    var isDoOpen: Boolean = false
    val isOpened: ImBool = ImBool(false)

    var checkOpenStatus: Boolean = false

    val mapSaveModes: List<MapSaveMode> = MapSaveMode.enums.map { it as MapSaveMode }
    val nudgeModes: List<NudgeMode> = NudgeMode.enums.map { it as NudgeMode }

    val preferencesByGroups: MutableMap<String, MutableList<Preference<Any>>> = mutableMapOf()
}
