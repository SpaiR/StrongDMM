package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class EventAvailableMapsDialogUi {
    class Open : Event<Unit, Unit>(Unit, null)
}
