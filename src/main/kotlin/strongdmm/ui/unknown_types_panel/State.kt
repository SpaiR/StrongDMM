package strongdmm.ui.unknown_types_panel

import imgui.ImBool
import strongdmm.byond.dmm.MapPos

class State {
    val isOpened: ImBool = ImBool(false)
    var unknownTypes: List<Pair<MapPos, String>> = emptyList()
}
