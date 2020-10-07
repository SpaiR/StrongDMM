package strongdmm.ui.panel.level_switch

import strongdmm.event.EventBus
import strongdmm.event.type.service.TriggerMapHolderService

class ViewController(
    private val state: State
) {
    fun doDecreaseSelectedZ() {
        EventBus.post(TriggerMapHolderService.ChangeSelectedZ(state.selectedMap!!.zSelected - 1))
    }

    fun doIncreaseSelectedZ() {
        EventBus.post(TriggerMapHolderService.ChangeSelectedZ(state.selectedMap!!.zSelected + 1))
    }

    fun isNotProcessable(): Boolean = state.selectedMap == null || state.selectedMap!!.maxZ == 1
}
