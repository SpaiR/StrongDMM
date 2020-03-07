package strongdmm.controller.tool.fill

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.controller.tool.Tool
import strongdmm.event.Event
import strongdmm.event.EventSender
import kotlin.math.max
import kotlin.math.min

class FillAddTool : Tool(), EventSender {
    private var xStart: Int = 0
    private var yStart: Int = 0

    private var x1: Int = 0
    private var y1: Int = 0
    private var x2: Int = 0
    private var y2: Int = 0

    private var usedTileItem: TileItem? = null
    private var currentMap: Dmm? = null

    override fun onStart(mapPos: MapPos) {
        isActive = currentMap != null && usedTileItem != null

        if (isActive) {
            xStart = mapPos.x
            yStart = mapPos.y
            fillAreaRect(mapPos.x, mapPos.y)
        }
    }

    override fun onStop() {
        isActive = false

        val reverseActions = mutableListOf<Undoable>()

        for (x in x1..x2) {
            for (y in y1..y2) {
                currentMap?.getTile(x, y)?.let { tile ->
                    reverseActions.add(ReplaceTileAction(tile) {
                        tile.addTileItem(usedTileItem!!)
                    })
                }
            }
        }

        if (reverseActions.isNotEmpty()) {
            sendEvent(Event.ActionController.AddAction(MultiAction(reverseActions)))
            sendEvent(Event.Global.RefreshFrame())
        }

        sendEvent(Event.CanvasController.ResetSelectedArea())
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        fillAreaRect(mapPos.x, mapPos.y)
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        usedTileItem = tileItem
    }

    override fun onMapSwitch(map: Dmm?) {
        currentMap = map
    }

    override fun reset() {
        isActive = false
        sendEvent(Event.CanvasController.ResetSelectedArea())
    }

    override fun destroy() {
        reset()
        usedTileItem = null
        currentMap = null
    }

    private fun fillAreaRect(x: Int, y: Int) {
        x1 = min(xStart, x)
        y1 = min(yStart, y)
        x2 = max(xStart, x)
        y2 = max(yStart, y)
        sendEvent(Event.CanvasController.SelectArea(MapArea(x1, y1, x2, y2)))
    }
}
