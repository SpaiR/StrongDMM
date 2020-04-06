package strongdmm.event.type.ui

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.ui.closemap.CloseMapDialogStatus

abstract class EventCloseMapDialogUi {
    class Open(body: Dmm, callback: ((CloseMapDialogStatus) -> Unit)) : Event<Dmm, CloseMapDialogStatus>(body, callback)
}
