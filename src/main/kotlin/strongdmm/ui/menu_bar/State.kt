package strongdmm.ui.menu_bar

import imgui.ImBool
import strongdmm.byond.dmm.MapPath

class State {
    lateinit var providedShowInstanceLocator: ImBool
    lateinit var providedFrameAreas: ImBool
    lateinit var providedRecentEnvironments: List<String>
    lateinit var providedRecentMaps: List<MapPath>

    var progressText: String? = null
    var isEnvironmentOpened: Boolean = false
    var isMapOpened: Boolean = false

    var isUndoEnabled: Boolean = false
    var isRedoEnabled: Boolean = false

    val isAreaLayerActive: ImBool = ImBool(true)
    val isTurfLayerActive: ImBool = ImBool(true)
    val isObjLayerActive: ImBool = ImBool(true)
    val isMobLayerActive: ImBool = ImBool(true)
}
