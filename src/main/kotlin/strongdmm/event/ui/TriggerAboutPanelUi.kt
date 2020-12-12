package strongdmm.event.ui

import strongdmm.event.Event

abstract class TriggerAboutPanelUi {
    class Open : Event<Unit, Unit>(Unit, null)
}
