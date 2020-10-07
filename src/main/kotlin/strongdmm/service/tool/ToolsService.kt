package strongdmm.service.tool

import strongdmm.application.Service
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerToolsService
import strongdmm.service.tool.select.SelectComplexTool
import strongdmm.service.tool.tile.TileComplexTool
import strongdmm.util.OUT_OF_BOUNDS

class ToolsService : Service {
    private var currentTool: Tool = TileComplexTool()
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    private var isMapOpened: Boolean = false
    private var selectedTileItem: TileItem? = null

    init {
        EventBus.sign(Reaction.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        EventBus.sign(Reaction.MapMouseDragStarted::class.java, ::handleMapMouseDragStarted)
        EventBus.sign(Reaction.MapMouseDragStopped::class.java, ::handleMapMouseDragStopped)
        EventBus.sign(Reaction.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.SelectedMapZSelectedChanged::class.java, ::handleSelectedMapZSelectedChanged)
        EventBus.sign(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(TriggerToolsService.ChangeTool::class.java, ::handleChangeTool)
        EventBus.sign(TriggerToolsService.ResetTool::class.java, ::handleResetTool)
        EventBus.sign(TriggerToolsService.FetchSelectedArea::class.java, ::handleFetchSelectedArea)
        EventBus.sign(TriggerToolsService.SelectArea::class.java, ::handleSelectArea)
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

    private fun handleSelectedTileItemChanged(event: Event<TileItem?, Unit>) {
        selectedTileItem = event.body
        currentTool.onTileItemSwitch(event.body)
    }

    private fun handleSelectedMapChanged() {
        isMapOpened = true
        currentTool.reset()
    }

    private fun handleSelectedMapZSelectedChanged() {
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
        currentTool.onTileItemSwitch(selectedTileItem)
        EventBus.post(Reaction.SelectedToolChanged(event.body))
    }

    private fun handleResetTool() {
        currentTool.reset()
    }

    private fun handleFetchSelectedArea(event: Event<Unit, MapArea>) {
        val selectedArea = if ((currentTool is SelectComplexTool) && currentTool.getSelectedArea().isNotOutOfBounds()) {
            currentTool.getSelectedArea()
        } else {
            MapArea(currentMapPos.x, currentMapPos.y, currentMapPos.x, currentMapPos.y)
        }

        if (selectedArea.isNotOutOfBounds()) {
            event.reply(selectedArea)
        }
    }

    private fun handleSelectArea(event: Event<MapArea, Unit>) {
        EventBus.post(TriggerToolsService.ChangeTool(ToolType.SELECT))
        (currentTool as SelectComplexTool).selectArea(event.body)
    }
}
