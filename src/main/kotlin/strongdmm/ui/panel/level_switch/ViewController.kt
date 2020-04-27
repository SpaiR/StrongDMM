package strongdmm.ui.panel.level_switch

import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerMapHolderController

class ViewController(
    private val state: State
) : EventHandler {
    fun doDecreaseActiveZ() {
        sendEvent(TriggerMapHolderController.ChangeActiveZ(state.selectedMap!!.zActive - 1))
    }

    fun doIncreaseActiveZ() {
        sendEvent(TriggerMapHolderController.ChangeActiveZ(state.selectedMap!!.zActive + 1))
    }

    fun isNotProcessable(): Boolean = state.selectedMap == null || state.selectedMap!!.maxZ == 1
}
