package strongdmm.ui.dialog.available_maps

import imgui.ImGui
import strongdmm.byond.dmm.MapPath
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerMapHolderController
import java.io.File

class ViewController(
    private val state: State
) : EventHandler {
    fun doSelectMapPath(mapPath: MapPath) {
        state.selectedMapPath = mapPath
    }

    fun doOpenSelectedMapAndDispose() {
        state.selectedMapPath?.let {
            sendEvent(TriggerMapHolderController.OpenMap(File(it.absolute)))
            dispose()
        }
    }

    fun isFilteredOutVisibleFilePath(visibleFilePath: String): Boolean {
        return state.mapFilter.length > 0 && !visibleFilePath.contains(state.mapFilter.get(), ignoreCase = true)
    }

    fun dispose() {
        ImGui.closeCurrentPopup()
        state.selectedMapPath = null
        sendEvent(Reaction.ApplicationBlockChanged(false))
    }
}
