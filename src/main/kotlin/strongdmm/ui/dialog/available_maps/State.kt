package strongdmm.ui.dialog.available_maps

import imgui.ImString
import strongdmm.byond.dmm.MapPath

class State {
    lateinit var providedAvailableMapPaths: Set<MapPath>

    var isDoOpen: Boolean = false
    var isFirstOpen: Boolean = true

    var selectedMapPath: MapPath? = null

    val mapFilter: ImString = ImString(10).apply { inputData.isResizable = true }
}
