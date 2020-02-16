package strongdmm.controller.tool

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.util.OUT_OF_BOUNDS

class ToolsController : EventConsumer {
    private var currentTool: Tool = AddDeleteTool()
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    init {
        consumeEvent(Event.Global.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(Event.Global.MapMouseDragStart::class.java, ::handleMapMouseDragStart)
        consumeEvent(Event.Global.MapMouseDragStop::class.java, ::handleMapMouseDragStop)
        consumeEvent(Event.Global.SwitchSelectedTileItem::class.java, ::handleSwitchSelectedTileItem)
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
        if (currentTool.isActive && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onMapPosChanged(currentMapPos)
        }
    }

    private fun handleMapMouseDragStart() {
        if (currentMapPos.x != OUT_OF_BOUNDS &&  currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onStart(currentMapPos)
        }
    }

    private fun handleMapMouseDragStop() {
        currentTool.onStop()
    }

    private fun handleSwitchSelectedTileItem(event: Event<TileItem, Unit>) {
        currentTool.onTileItemSwitch(event.body)
    }

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        currentTool.onMapSwitch(event.body)
    }
}
