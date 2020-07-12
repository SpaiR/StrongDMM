package strongdmm.service.preferences

import com.fasterxml.jackson.annotation.JsonIgnore
import strongdmm.service.preferences.prefs.Preference
import strongdmm.service.preferences.prefs.bools.AlternativeScrollBehavior
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
    val interfaceScalePercent = InterfaceScalePercent()
    val styleMode = StyleMode()

    // Controls Options
    val alternativeScrollBehavior = AlternativeScrollBehavior()

    // Save Options
    val mapSaveMode = MapSaveMode()
    val sanitizeInitialVariables = SanitizeInitialVariables()
    val cleanUnusedKeys = CleanUnusedKeys()
    val nudgeMode = NudgeMode()
}
