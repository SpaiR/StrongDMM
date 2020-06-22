package strongdmm.service.preferences

import com.fasterxml.jackson.annotation.JsonIgnore
import strongdmm.service.preferences.prefs.Preference
import strongdmm.service.preferences.prefs.bools.CleanUnusedKeys
import strongdmm.service.preferences.prefs.bools.SanitizeInitialVariables
import strongdmm.service.preferences.prefs.enums.MapSaveMode
import strongdmm.service.preferences.prefs.enums.NudgeMode
import strongdmm.service.preferences.prefs.enums.StyleMode
import strongdmm.service.preferences.prefs.ints.InterfaceScalePercent

class Preferences {
    @JsonIgnore
    var rawValues: List<Preference<Any>> = emptyList()

    // Interface Options
    val interfaceScalePercent: InterfaceScalePercent = InterfaceScalePercent()
    val styleMode: StyleMode = StyleMode()

    // Save Options
    val mapSaveMode: MapSaveMode = MapSaveMode()
    val sanitizeInitialVariables: SanitizeInitialVariables = SanitizeInitialVariables()
    val cleanUnusedKeys: CleanUnusedKeys = CleanUnusedKeys()
    val nudgeMode: NudgeMode = NudgeMode()
}
