package strongdmm.ui.panel.unknown_types

import imgui.type.ImBoolean
import strongdmm.byond.dmm.MapPos

class State {
    val isOpened: ImBoolean = ImBoolean(false)
    var unknownTypes: List<Pair<MapPos, String>> = emptyList()
}
