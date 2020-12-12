package strongdmm.ui.dialog.available_maps

import imgui.ImGui
import strongdmm.byond.dmm.MapPath
import strongdmm.event.EventBus
import strongdmm.event.Reaction
import strongdmm.event.service.TriggerMapHolderService
import java.io.File

class ViewController(
    private val state: State
) {
    fun doSelectMapPath(mapPath: MapPath) {
        state.selectedMapPath = mapPath
    }

    fun doOpenSelectedMapAndDispose() {
        state.selectedMapPath?.let {
            EventBus.post(TriggerMapHolderService.OpenMap(File(it.absolute)))
            dispose()
        }
    }

    fun isFilteredOutVisibleFilePath(visibleFilePath: String): Boolean {
        return state.mapFilter.length > 0 && !visibleFilePath.contains(state.mapFilter.get(), ignoreCase = true)
    }

    fun dispose() {
        ImGui.closeCurrentPopup()
        state.selectedMapPath = null
        EventBus.post(Reaction.ApplicationBlockChanged(false))
    }
}
