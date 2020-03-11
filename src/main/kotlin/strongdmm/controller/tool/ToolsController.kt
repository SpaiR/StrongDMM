package strongdmm.controller.tool

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.tool.select.SelectComplexTool
import strongdmm.controller.tool.tile.TileComplexTool
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.OUT_OF_BOUNDS

class ToolsController : EventConsumer, EventSender {
    private var currentTool: Tool = TileComplexTool()
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    private var currentMapId: Int = Dmm.MAP_ID_NONE
    private var selectedTileItem: TileItem? = null

    init {
        consumeEvent(Event.Global.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(Event.Global.MapMouseDragStart::class.java, ::handleMapMouseDragStart)
        consumeEvent(Event.Global.MapMouseDragStop::class.java, ::handleMapMouseDragStop)
        consumeEvent(Event.Global.SwitchSelectedTileItem::class.java, ::handleSwitchSelectedTileItem)
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.ToolsController.Switch::class.java, ::handleSwitch)
        consumeEvent(Event.ToolsController.Reset::class.java, ::handleReset)
        consumeEvent(Event.ToolsController.FetchActiveArea::class.java, ::handleFetchActiveArea)
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
        if (currentTool.isActive && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onMapPosChanged(currentMapPos)
        }
    }

    private fun handleMapMouseDragStart() {
        if (currentMapId != Dmm.MAP_ID_NONE && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onStart(currentMapPos)
        }
    }

    private fun handleMapMouseDragStop() {
        if (currentTool.isActive) {
            currentTool.onStop()
        }
    }

    private fun handleSwitchSelectedTileItem(event: Event<TileItem, Unit>) {
        selectedTileItem = event.body
        currentTool.onTileItemSwitch(event.body)
    }

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        currentTool.reset()
        currentMapId = event.body.id
    }

    private fun handleCloseMap(event: Event<Dmm, Unit>) {
        if (currentMapId == event.body.id) {
            currentTool.reset()
            currentMapId = Dmm.MAP_ID_NONE
        }
    }

    private fun handleResetEnvironment() {
        currentMapId = Dmm.MAP_ID_NONE
        selectedTileItem = null
        currentTool.destroy()
        currentTool.onTileItemSwitch(null)
    }

    private fun handleSwitch(event: Event<ToolType, Unit>) {
        currentTool.destroy()
        currentTool = event.body.createTool()
        currentTool.onTileItemSwitch(selectedTileItem)
        sendEvent(Event.Global.SwitchUsedTool(event.body))
    }

    private fun handleReset() {
        currentTool.reset()
    }

    private fun handleFetchActiveArea(event: Event<Unit, MapArea>) {
        val activeArea = if (currentTool is SelectComplexTool) {
            currentTool.getActiveArea()
        } else {
            MapArea(currentMapPos.x, currentMapPos.y, currentMapPos.x, currentMapPos.y)
        }

        if (activeArea.isNotOutOfBounds()) {
            event.reply(activeArea)
        }
    }
}
