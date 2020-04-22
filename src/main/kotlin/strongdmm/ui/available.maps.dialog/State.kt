package strongdmm.ui.available.maps.dialog

import imgui.ImString

class State {
    var isDoOpen: Boolean = false
    var isFirstOpen: Boolean = true

    var providedAvailableMaps: Set<Pair<String, String>> = emptySet()

    var selectedAbsMapPath: String? = null // to store an absolute path for currently selected map
    var selectionStatus: String = "" // to display a currently selected map (visible path)

    val mapFilter: ImString = ImString(10).apply { inputData.isResizable = true }
}
