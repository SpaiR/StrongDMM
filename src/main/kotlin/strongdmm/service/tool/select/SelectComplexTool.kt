package strongdmm.service.tool.select

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerCanvasService
import strongdmm.service.tool.ComplexTool
import strongdmm.service.tool.Tool

class SelectComplexTool : ComplexTool(), EventHandler {
    private val add = SelectAddAreaTool()
    private val move = SelectMoveAreaTool()

    override var currentTool: Tool = add

    override fun onStart(mapPos: MapPos) {
        if (currentTool is SelectMoveAreaTool && !move.currentSelectedArea.isInBounds(mapPos.x, mapPos.y)) {
            currentTool = add
        }

        currentTool.onStart(mapPos)
    }

    override fun onStop() {
        currentTool.onStop()

        if (currentTool is SelectAddAreaTool) {
            currentTool = move
            move.currentSelectedArea = add.currentSelectedArea
        } else {
            add.currentSelectedArea = move.currentSelectedArea
        }
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        currentTool.onMapPosChanged(mapPos)
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        // unused
    }

    override fun getSelectedArea(): MapArea = currentTool.getSelectedArea()

    override fun reset() {
        add.reset()
        move.reset()
        currentTool = add
    }

    override fun destroy() {
        add.destroy()
        move.destroy()
    }

    fun selectArea(area: MapArea) {
        reset()
        add.currentSelectedArea = area
        onStop()
        sendEvent(TriggerCanvasService.SelectArea(area))
    }
}
