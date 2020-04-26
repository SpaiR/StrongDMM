package strongdmm.ui.available_maps_dialog

import imgui.ImString

class State {
    lateinit var providedAvailableMaps: Set<Pair<String, String>>

    var isDoOpen: Boolean = false
    var isFirstOpen: Boolean = true

    var selectedAbsMapPath: String? = null // to store an absolute path for currently selected map
    var selectionStatus: String = "" // to display a currently selected map (visible path)

    val mapFilter: ImString = ImString(10).apply { inputData.isResizable = true }
}
