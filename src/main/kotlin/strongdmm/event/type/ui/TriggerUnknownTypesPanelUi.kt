package strongdmm.event.type.ui

import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event

abstract class TriggerUnknownTypesPanelUi {
    class Open(body: List<Pair<MapPos, String>>) : Event<List<Pair<MapPos, String>>, Unit>(body, null)
}
