package strongdmm.controller.tool

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.tool.select.SelectComplexTool
import strongdmm.controller.tool.tile.TileComplexTool
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerToolsController
import strongdmm.util.OUT_OF_BOUNDS

class ToolsController : EventConsumer, EventSender {
    private var currentTool: Tool = TileComplexTool()
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    private var isMapOpened: Boolean = false
    private var activeTileItem: TileItem? = null

    init {
        consumeEvent(Reaction.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(Reaction.MapMouseDragStarted::class.java, ::handleMapMouseDragStarted)
        consumeEvent(Reaction.MapMouseDragStopped::class.java, ::handleMapMouseDragStopped)
        consumeEvent(Reaction.SelectedTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapZActiveChanged::class.java, ::handleSelectedMapZActiveChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(TriggerToolsController.ChangeTool::class.java, ::handleChangeTool)
        consumeEvent(TriggerToolsController.ResetTool::class.java, ::handleResetTool)
        consumeEvent(TriggerToolsController.FetchActiveArea::class.java, ::handleFetchActiveArea)
        consumeEvent(TriggerToolsController.SelectActiveArea::class.java, ::handleSelectActiveArea)
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
        if (currentTool.isActive && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onMapPosChanged(currentMapPos)
        }
    }

    private fun handleMapMouseDragStarted() {
        if (isMapOpened && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
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

    private fun handleSelectedMapChanged() {
        isMapOpened = true
        currentTool.reset()
    }

    private fun handleSelectedMapZActiveChanged() {
        currentTool.reset()
    }

    private fun handleSelectedMapClosed() {
        isMapOpened = false
        currentTool.reset()
    }

    private fun handleEnvironmentReset() {
        isMapOpened = false
        currentTool.destroy()
        currentTool.onTileItemSwitch(null)
    }

    private fun handleChangeTool(event: Event<ToolType, Unit>) {
        currentTool.destroy()
        currentTool = event.body.createTool()
        currentTool.onTileItemSwitch(activeTileItem)
        sendEvent(Reaction.SelectedToolChanged(event.body))
    }

    private fun handleResetTool() {
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

    private fun handleSelectActiveArea(event: Event<MapArea, Unit>) {
        sendEvent(TriggerToolsController.ChangeTool(ToolType.SELECT))
        (currentTool as SelectComplexTool).selectArea(event.body)
    }
}
