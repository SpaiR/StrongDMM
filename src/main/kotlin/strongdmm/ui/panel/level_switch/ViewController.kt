package strongdmm.ui.panel.level_switch

import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerMapHolderController

class ViewController(
    private val state: State
) : EventHandler {
    fun doDecreaseActiveZ() {
        sendEvent(TriggerMapHolderController.ChangeSelectedZ(state.selectedMap!!.zSelected - 1))
    }

    fun doIncreaseActiveZ() {
        sendEvent(TriggerMapHolderController.ChangeSelectedZ(state.selectedMap!!.zSelected + 1))
    }

    fun isNotProcessable(): Boolean = state.selectedMap == null || state.selectedMap!!.maxZ == 1
}
