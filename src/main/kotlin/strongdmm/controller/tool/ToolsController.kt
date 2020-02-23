package strongdmm.controller.tool

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.tool.tile.TileComplexTool
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.OUT_OF_BOUNDS

class ToolsController : EventConsumer, EventSender {
    private var currentTool: Tool = TileComplexTool()
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    private var currentMap: Dmm? = null
    private var selectedTileItem: TileItem? = null

    init {
        consumeEvent(Event.Global.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(Event.Global.MapMouseDragStart::class.java, ::handleMapMouseDragStart)
        consumeEvent(Event.Global.MapMouseDragStop::class.java, ::handleMapMouseDragStop)
        consumeEvent(Event.Global.SwitchSelectedTileItem::class.java, ::handleSwitchSelectedTileItem)
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.ToolsController.Switch::class.java, ::handleSwitch)
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
        if (currentTool.isActive && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onMapPosChanged(currentMapPos)
        }
    }

    private fun handleMapMouseDragStart() {
        if (currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onStart(currentMapPos)
        }
    }

    private fun handleMapMouseDragStop() {
        currentTool.onStop()
    }

    private fun handleSwitchSelectedTileItem(event: Event<TileItem, Unit>) {
        selectedTileItem = event.body
        currentTool.onTileItemSwitch(event.body)
    }

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        currentMap = event.body
        currentTool.onMapSwitch(event.body)
    }

    private fun handleResetEnvironment() {
        currentMap = null
        selectedTileItem = null
        currentTool.onTileItemSwitch(null)
        currentTool.onMapSwitch(null)
    }

    private fun handleSwitch(event: Event<ToolType, Unit>) {
        currentTool = event.body.createTool()
        currentTool.onMapSwitch(currentMap)
        currentTool.onTileItemSwitch(selectedTileItem)
        sendEvent(Event.Global.SwitchUsedTool(event.body))
    }
}
