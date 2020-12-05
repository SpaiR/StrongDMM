package strongdmm.ui.panel.openedmaps

import strongdmm.byond.dmm.Dmm
import strongdmm.event.EventBus
import strongdmm.event.type.service.TriggerMapHolderService

class ViewController(
    private val state: State
) {
    fun doCloseMap(map: Dmm) {
        EventBus.post(TriggerMapHolderService.CloseMap(map.id))
    }

    fun doOpenMap(map: Dmm) {
        if (state.selectedMap !== map) {
            EventBus.post(TriggerMapHolderService.ChangeSelectedMap(map.id))
        }
    }

    fun isSelectedMap(map: Dmm): Boolean = state.selectedMap === map

    fun isModifiedMap(map: Dmm): Boolean = state.providedActionBalanceStorage.isMapModified(map)
}
