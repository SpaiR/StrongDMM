package strongdmm.service.tool.fill

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.service.action.undoable.MultiAction
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.service.action.undoable.Undoable
import strongdmm.service.tool.Tool
import strongdmm.event.EventSender
import strongdmm.event.type.controller.TriggerActionController
import strongdmm.event.type.controller.TriggerCanvasController
import strongdmm.event.type.controller.TriggerFrameController
import strongdmm.event.type.controller.TriggerMapHolderController
import kotlin.math.max
import kotlin.math.min

class FillAddTool : Tool(), EventSender {
    private var xStart: Int = 0
    private var yStart: Int = 0

    private var x1: Int = 0
    private var y1: Int = 0
    private var x2: Int = 0
    private var y2: Int = 0

    private var selectedTileItem: TileItem? = null

    override fun onStart(mapPos: MapPos) {
        isActive = selectedTileItem != null

        if (isActive) {
            xStart = mapPos.x
            yStart = mapPos.y
            fillAreaRect(mapPos.x, mapPos.y)
        }
    }

    override fun onStop() {
        isActive = false

        val reverseActions = mutableListOf<Undoable>()

        sendEvent(TriggerMapHolderController.FetchSelectedMap { selectedMap ->
            for (x in x1..x2) {
                for (y in y1..y2) {
                    val tile = selectedMap.getTile(x, y, selectedMap.zSelected)

                    reverseActions.add(ReplaceTileAction(tile) {
                        tile.addTileItem(selectedTileItem!!)
                    })
                }
            }
        })

        if (reverseActions.isNotEmpty()) {
            sendEvent(TriggerActionController.AddAction(MultiAction(reverseActions)))
            sendEvent(TriggerFrameController.RefreshFrame())
        }

        sendEvent(TriggerCanvasController.ResetSelectedArea())
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        fillAreaRect(mapPos.x, mapPos.y)
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        selectedTileItem = tileItem
    }

    override fun reset() {
        isActive = false
        sendEvent(TriggerCanvasController.ResetSelectedArea())
    }

    override fun destroy() {
        reset()
        selectedTileItem = null
    }

    private fun fillAreaRect(x: Int, y: Int) {
        x1 = min(xStart, x)
        y1 = min(yStart, y)
        x2 = max(xStart, x)
        y2 = max(yStart, y)
        sendEvent(TriggerCanvasController.SelectArea(MapArea(x1, y1, x2, y2)))
    }
}
