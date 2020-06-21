package strongdmm.service.preferences

import imgui.ImBool
import imgui.ImInt
import strongdmm.service.preferences.model.MapSaveMode
import strongdmm.service.preferences.model.NudgeMode

class Preferences {
    // Interface Options
    var interfaceScalePercent: ImInt = ImInt(100)

    // Save Options
    var mapSaveMode: MapSaveMode = MapSaveMode.PROVIDED
    var sanitizeInitialVariables: ImBool = ImBool(false)
    var cleanUnusedKeys: ImBool = ImBool(true)
    var nudgeMode: NudgeMode = NudgeMode.PIXEL
}
