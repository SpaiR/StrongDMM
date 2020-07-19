package strongdmm.ui.dialog.confirmation

import strongdmm.event.Event
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogData
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogStatus

class State {
    var isDoOpen: Boolean = false

    var data: ConfirmationDialogData = ConfirmationDialogData()
    var eventToReply: Event<ConfirmationDialogData, ConfirmationDialogStatus>? = null
}
