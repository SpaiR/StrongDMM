package strongdmm.event.type.controller

import strongdmm.byond.dmm.MapArea
import strongdmm.service.tool.ToolType
import strongdmm.event.Event

abstract class TriggerToolsController {
    class ChangeTool(body: ToolType) : Event<ToolType, Unit>(body, null)
    class ResetTool : Event<Unit, Unit>(Unit, null)
    class FetchSelectedArea(callback: ((MapArea) -> Unit)) : Event<Unit, MapArea>(Unit, callback)
    class SelectArea(body: MapArea) : Event<MapArea, Unit>(body, null)
}
