package strongdmm.ui.dialog.close_map

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.ui.dialog.close_map.model.CloseMapDialogStatus

class State {
    var isDoOpen: Boolean = false

    var eventToReply: Event<Dmm, CloseMapDialogStatus>? = null
}
