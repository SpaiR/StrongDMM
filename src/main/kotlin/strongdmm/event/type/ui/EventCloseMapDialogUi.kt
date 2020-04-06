package strongdmm.event.type.ui

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.ui.closemap.CloseMapStatus

abstract class EventCloseMapDialogUi {
    class Open(body: Dmm, callback: ((CloseMapStatus) -> Unit)) : Event<Dmm, CloseMapStatus>(body, callback)
}
