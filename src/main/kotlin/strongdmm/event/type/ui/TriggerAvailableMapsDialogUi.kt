package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class TriggerAvailableMapsDialogUi {
    class Open : Event<Unit, Unit>(Unit, null)
}
