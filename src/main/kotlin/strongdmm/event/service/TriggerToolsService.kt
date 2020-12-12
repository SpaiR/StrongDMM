package strongdmm.event.service

import strongdmm.byond.dmm.MapArea
import strongdmm.event.Event
import strongdmm.service.tool.ToolType

abstract class TriggerToolsService {
    class ChangeTool(body: ToolType) : Event<ToolType, Unit>(body, null)
    class ResetTool : Event<Unit, Unit>(Unit, null)
    class FetchSelectedArea(callback: ((MapArea) -> Unit)) : Event<Unit, MapArea>(Unit, callback)
    class SelectArea(body: MapArea) : Event<MapArea, Unit>(body, null)
}
