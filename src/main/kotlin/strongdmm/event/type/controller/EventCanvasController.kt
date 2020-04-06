package strongdmm.event.type.controller

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event

abstract class EventCanvasController {
    class CenterCanvasByPosition(body: MapPos) : Event<MapPos, Unit>(body, null)
    class MarkPosition(body: MapPos) : Event<MapPos, Unit>(body, null)
    class ResetMarkedPosition : Event<Unit, Unit>(Unit, null)
    class SelectTiles(body: Collection<MapPos>) : Event<Collection<MapPos>, Unit>(body, null)
    class ResetSelectedTiles : Event<Unit, Unit>(Unit, null)
    class SelectArea(body: MapArea) : Event<MapArea, Unit>(body, null)
    class ResetSelectedArea : Event<Unit, Unit>(Unit, null)
}
