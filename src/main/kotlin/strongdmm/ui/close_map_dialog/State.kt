package strongdmm.ui.close_map_dialog

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.ui.close_map_dialog.model.CloseMapDialogStatus

class State {
    var isDoOpen: Boolean = false

    var eventToReply: Event<Dmm, CloseMapDialogStatus>? = null
}
