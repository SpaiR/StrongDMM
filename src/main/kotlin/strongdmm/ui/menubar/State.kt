package strongdmm.ui.menubar

import imgui.type.ImBoolean
import strongdmm.byond.dmm.MapPath

class State {
    lateinit var providedDoInstanceLocatorOpen: ImBoolean
    lateinit var providedDoFrameAreas: ImBoolean
    lateinit var providedDoSynchronizeMapsView: ImBoolean
    lateinit var providedRecentEnvironments: List<String>
    lateinit var providedRecentMaps: List<MapPath>

    var isLoadingEnvironment: Boolean = false
    var isEnvironmentOpened: Boolean = false
    var isMapOpened: Boolean = false

    var isUndoEnabled: Boolean = false
    var isRedoEnabled: Boolean = false

    val isAreaLayerActive: ImBoolean = ImBoolean(true)
    val isTurfLayerActive: ImBoolean = ImBoolean(true)
    val isObjLayerActive: ImBoolean = ImBoolean(true)
    val isMobLayerActive: ImBoolean = ImBoolean(true)
}
