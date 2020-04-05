package strongdmm.controller.preferences

import imgui.ImBool

class Preferences {
    var mapSaveMode: MapSaveMode = MapSaveMode.PROVIDED
    var sanitizeInitialVariables: ImBool = ImBool(false)
    var cleanUnusedKeys: ImBool = ImBool(true)
}
