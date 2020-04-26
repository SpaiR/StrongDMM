package strongdmm.ui.available_maps_dialog

import imgui.ImGui
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerMapHolderController
import java.io.File

class ViewController(
    private val state: State
) : EventHandler {
    fun doSelectMapPath(absoluteFilePath: String, visibleFilePath: String) {
        state.selectedAbsMapPath = absoluteFilePath
        state.selectionStatus = visibleFilePath
    }

    fun doOpenSelectedMapAndDispose() {
        state.selectedAbsMapPath?.let {
            sendEvent(TriggerMapHolderController.OpenMap(File(it)))
            dispose()
        }
    }

    fun isFilteredOutVisibleFilePath(visibleFilePath: String): Boolean {
        return state.mapFilter.length > 0 && !visibleFilePath.contains(state.mapFilter.get(), ignoreCase = true)
    }

    fun dispose() {
        ImGui.closeCurrentPopup()
        state.selectedAbsMapPath = null
        state.selectionStatus = ""
        sendEvent(Reaction.ApplicationBlockChanged(false))
    }
}
