package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class EventLayersFilterPanelUi {
    class Open : Event<Unit, Unit>(Unit, null)
}
