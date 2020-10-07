package strongdmm.service.tool.fill

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventBus
import strongdmm.event.type.service.TriggerActionService
import strongdmm.event.type.service.TriggerCanvasService
import strongdmm.event.type.service.TriggerFrameService
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.service.action.undoable.MultiAction
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.service.action.undoable.Undoable
import strongdmm.service.tool.Tool
import kotlin.math.max
import kotlin.math.min

class FillAddTool : Tool() {
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

        EventBus.post(TriggerMapHolderService.FetchSelectedMap { selectedMap ->
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
            EventBus.post(TriggerActionService.QueueUndoable(MultiAction(reverseActions)))
            EventBus.post(TriggerFrameService.RefreshFrame())
        }

        EventBus.post(TriggerCanvasService.ResetSelectedArea())
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        fillAreaRect(mapPos.x, mapPos.y)
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        selectedTileItem = tileItem
    }

    override fun reset() {
        isActive = false
        EventBus.post(TriggerCanvasService.ResetSelectedArea())
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
        EventBus.post(TriggerCanvasService.SelectArea(MapArea(x1, y1, x2, y2)))
    }
}
