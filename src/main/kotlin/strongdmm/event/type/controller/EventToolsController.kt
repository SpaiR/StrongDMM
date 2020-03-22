package strongdmm.event.type.controller

import strongdmm.byond.dmm.MapArea
import strongdmm.controller.tool.ToolType
import strongdmm.event.Event

abstract class EventToolsController {
    class Change(body: ToolType) : Event<ToolType, Unit>(body, null)
    class Reset : Event<Unit, Unit>(Unit, null)
    class FetchActiveArea(callback: ((MapArea) -> Unit)) : Event<Unit, MapArea>(Unit, callback)
    class SelectActiveArea(body: MapArea) : Event<MapArea, Unit>(body, null)
}
