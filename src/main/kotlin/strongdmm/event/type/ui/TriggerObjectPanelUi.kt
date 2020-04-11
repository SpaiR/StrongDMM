package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class TriggerObjectPanelUi {
    class Update : Event<Unit, Unit>(Unit, null)
}
