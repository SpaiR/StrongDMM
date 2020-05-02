package strongdmm.event.type.ui

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.ui.dialog.close_map.model.CloseMapDialogStatus

abstract class TriggerCloseMapDialogUi {
    class Open(body: Dmm, callback: ((CloseMapDialogStatus) -> Unit)) : Event<Dmm, CloseMapDialogStatus>(body, callback)
}
