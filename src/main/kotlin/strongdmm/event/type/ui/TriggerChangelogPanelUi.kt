package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class TriggerChangelogPanelUi {
    class Open : Event<Unit, Unit>(Unit, null)
}
