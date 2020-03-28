package strongdmm.controller.tool.select

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.tool.Tool
import strongdmm.event.EventSender
import strongdmm.event.type.controller.EventCanvasController
import kotlin.math.max
import kotlin.math.min

class SelectAddAreaTool : Tool(), EventSender {
    private var xStart: Int = 0
    private var yStart: Int = 0

    var selectedArea: MapArea = MapArea.OUT_OF_BOUNDS_AREA

    override fun onStart(mapPos: MapPos) {
        isActive = true

        if (isActive) {
            xStart = mapPos.x
            yStart = mapPos.y
            fillAreaRect(mapPos.x, mapPos.y)
            sendEvent(EventCanvasController.HighlightSelectedArea())
        }
    }

    override fun onStop() {
        isActive = false
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        fillAreaRect(mapPos.x, mapPos.y)
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        // unused
    }

    override fun getActiveArea(): MapArea = selectedArea

    override fun reset() {
        isActive = false
        selectedArea = MapArea.OUT_OF_BOUNDS_AREA
        sendEvent(EventCanvasController.ResetSelectedArea())
    }

    override fun destroy() {
        reset()
    }

    private fun fillAreaRect(x: Int, y: Int) {
        val x1 = min(xStart, x)
        val y1 = min(yStart, y)
        val x2 = max(xStart, x)
        val y2 = max(yStart, y)
        selectedArea = MapArea(x1, y1, x2, y2)
        sendEvent(EventCanvasController.SelectArea(selectedArea))
    }
}
