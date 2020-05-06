package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class TriggerScreenshotPanelUi {
    class Open : Event<Unit, Unit>(Unit, null)
}
