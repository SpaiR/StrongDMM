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

    fun getMapName(map: Dmm): String {
        return (if (state.providedActionBalanceStorage[map] != 0) "* " else "") + map.mapName
    }
}
