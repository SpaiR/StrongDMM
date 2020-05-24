package strongdmm.ui.panel.opened_maps

import strongdmm.byond.dmm.Dmm
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerMapHolderService

class ViewController(
    private val state: State
) : EventHandler {
    fun doCloseMap(map: Dmm) {
        sendEvent(TriggerMapHolderService.CloseMap(map.id))
    }

    fun doOpenMap(map: Dmm) {
        if (state.selectedMap !== map) {
            sendEvent(TriggerMapHolderService.ChangeSelectedMap(map.id))
        }
    }

    fun isSelectedMap(map: Dmm): Boolean = state.selectedMap === map

    fun isModifiedMap(map: Dmm): Boolean = state.providedActionBalanceStorage[map] != 0
}
