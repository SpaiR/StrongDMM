package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class TriggerLayersFilterPanelUi {
    class Open : Event<Unit, Unit>(Unit, null)
}
