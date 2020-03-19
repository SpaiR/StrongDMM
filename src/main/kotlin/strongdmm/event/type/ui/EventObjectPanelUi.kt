package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class EventObjectPanelUi {
    class Update : Event<Unit, Unit>(Unit, null)
}
