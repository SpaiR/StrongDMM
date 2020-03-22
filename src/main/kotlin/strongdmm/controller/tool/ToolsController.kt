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
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.EventToolsController
import strongdmm.util.OUT_OF_BOUNDS

class ToolsController : EventConsumer, EventSender {
    private var currentTool: Tool = TileComplexTool()
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    private var selectedMapId: Int = Dmm.MAP_ID_NONE
    private var activeTileItem: TileItem? = null

    init {
        consumeEvent(EventGlobal.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(EventGlobal.MapMouseDragStarted::class.java, ::handleMapMouseDragStarted)
        consumeEvent(EventGlobal.MapMouseDragStopped::class.java, ::handleMapMouseDragStopped)
        consumeEvent(EventGlobal.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(EventGlobal.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventToolsController.Change::class.java, ::handleChange)
        consumeEvent(EventToolsController.Reset::class.java, ::handleReset)
        consumeEvent(EventToolsController.FetchActiveArea::class.java, ::handleFetchActiveArea)
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
        if (currentTool.isActive && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onMapPosChanged(currentMapPos)
        }
    }

    private fun handleMapMouseDragStarted() {
        if (selectedMapId != Dmm.MAP_ID_NONE && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onStart(currentMapPos)
        }
    }

    private fun handleMapMouseDragStopped() {
        if (currentTool.isActive) {
            currentTool.onStop()
        }
    }

    private fun handleActiveTileItemChanged(event: Event<TileItem?, Unit>) {
        activeTileItem = event.body
        currentTool.onTileItemSwitch(event.body)
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        currentTool.reset()
        selectedMapId = event.body.id
    }

    private fun handleOpenedMapClosed(event: Event<Dmm, Unit>) {
        if (selectedMapId == event.body.id) {
            currentTool.reset()
            selectedMapId = Dmm.MAP_ID_NONE
        }
    }

    private fun handleEnvironmentReset() {
        selectedMapId = Dmm.MAP_ID_NONE
        currentTool.destroy()
        currentTool.onTileItemSwitch(null)
    }

    private fun handleChange(event: Event<ToolType, Unit>) {
        currentTool.destroy()
        currentTool = event.body.createTool()
        currentTool.onTileItemSwitch(activeTileItem)
        sendEvent(EventGlobal.ActiveToolChanged(event.body))
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
