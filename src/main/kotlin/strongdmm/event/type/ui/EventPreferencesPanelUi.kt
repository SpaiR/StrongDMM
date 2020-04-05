package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class EventPreferencesPanelUi {
    class Open : Event<Unit, Unit>(Unit, null)
}
