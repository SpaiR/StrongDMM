package strongdmm.controller.tool.select

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.tool.ComplexTool
import strongdmm.controller.tool.Tool
import strongdmm.event.EventSender
import strongdmm.event.type.controller.EventCanvasController

class SelectComplexTool : ComplexTool(), EventSender {
    private val add = SelectAddAreaTool()
    private val move = SelectMoveAreaTool()

    override var currentTool: Tool = add

    override fun onStart(mapPos: MapPos) {
        if (currentTool is SelectMoveAreaTool && !move.selectedArea.isInBounds(mapPos.x, mapPos.y)) {
            currentTool = add
        }

        currentTool.onStart(mapPos)
    }

    override fun onStop() {
        currentTool.onStop()

        if (currentTool is SelectAddAreaTool) {
            currentTool = move
            move.selectedArea = add.selectedArea
        } else {
            add.selectedArea = move.selectedArea
        }
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        currentTool.onMapPosChanged(mapPos)
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        // unused
    }

    override fun getActiveArea(): MapArea = currentTool.getActiveArea()

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
        add.selectedArea = area
        onStop()
        sendEvent(EventCanvasController.SelectArea(area))
    }
}
