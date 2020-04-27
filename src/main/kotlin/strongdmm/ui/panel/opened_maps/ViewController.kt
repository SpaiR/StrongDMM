package strongdmm.ui.panel.opened_maps

import strongdmm.byond.dmm.Dmm
import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerMapHolderController

class ViewController(
    private val state: State
) : EventHandler {
    fun doCloseMap(map: Dmm) {
        sendEvent(TriggerMapHolderController.CloseMap(map.id))
    }

    fun doOpenMap(map: Dmm) {
        if (state.selectedMap !== map) {
            sendEvent(TriggerMapHolderController.ChangeSelectedMap(map.id))
        }
    }

    fun getMapName(map: Dmm): String {
        return map.mapName + if (state.providedActionBalanceStorage[map] != 0) " *" else ""
    }
}
