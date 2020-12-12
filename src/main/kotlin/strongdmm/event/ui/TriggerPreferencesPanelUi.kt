package strongdmm.event.ui

import strongdmm.event.Event

abstract class TriggerPreferencesPanelUi {
    class Open : Event<Unit, Unit>(Unit, null)
}
