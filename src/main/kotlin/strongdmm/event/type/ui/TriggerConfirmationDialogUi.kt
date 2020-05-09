package strongdmm.event.type.ui

import strongdmm.event.Event
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogData
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogStatus

abstract class TriggerConfirmationDialogUi {
    class Open(body: ConfirmationDialogData, callback: ((ConfirmationDialogStatus) -> Unit)) :
        Event<ConfirmationDialogData, ConfirmationDialogStatus>(body, callback)
}
