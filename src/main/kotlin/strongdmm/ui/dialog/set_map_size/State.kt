package strongdmm.ui.dialog.set_map_size

import imgui.ImInt

class State {
    var isDoOpen: Boolean = false

    val newX: ImInt = ImInt(0)
    val newY: ImInt = ImInt(0)
    val newZ: ImInt = ImInt(0)
}
