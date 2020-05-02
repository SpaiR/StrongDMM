package strongdmm.service.tool.fill

import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.service.*
import strongdmm.service.action.undoable.MultiAction
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.service.action.undoable.Undoable
import strongdmm.service.tool.Tool
import kotlin.math.max
import kotlin.math.min

class FillDeleteTool : Tool(), EventHandler {
    private var xStart: Int = 0
    private var yStart: Int = 0

    private var x1: Int = 0
    private var y1: Int = 0
    private var x2: Int = 0
    private var y2: Int = 0

    private var tileItemTypeToDelete: String? = null

    override fun onStart(mapPos: MapPos) {
        isActive = tileItemTypeToDelete != null

        if (isActive) {
            xStart = mapPos.x
            yStart = mapPos.y
            fillAreaRect(mapPos.x, mapPos.y)
        }
    }

    override fun onStop() {
        isActive = false

        val reverseActions = mutableListOf<Undoable>()

        sendEvent(TriggerMapHolderService.FetchSelectedMap { selectedMap ->
            sendEvent(TriggerLayersFilterService.FetchFilteredLayers { filteredTypes ->
                for (x in x1..x2) {
                    for (y in y1..y2) {
                        val tile = selectedMap.getTile(x, y, selectedMap.zSelected)

                        tile.getFilteredTileItems(filteredTypes).findLast { it.isType(tileItemTypeToDelete!!) }?.let { tileItem ->
                            reverseActions.add(ReplaceTileAction(tile) {
                                tile.deleteTileItem(tileItem)
                            })
                        }
                    }
                }
            })
        })

        if (reverseActions.isNotEmpty()) {
            sendEvent(TriggerActionService.AddAction(MultiAction(reverseActions)))
            sendEvent(TriggerFrameService.RefreshFrame())
        }

        sendEvent(TriggerCanvasService.ResetSelectedArea())
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        fillAreaRect(mapPos.x, mapPos.y)
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        tileItemTypeToDelete = when {
            tileItem == null -> ""
            tileItem.isType(TYPE_AREA) -> TYPE_AREA
            tileItem.isType(TYPE_TURF) -> TYPE_TURF
            tileItem.isType(TYPE_OBJ) -> TYPE_OBJ
            tileItem.isType(TYPE_MOB) -> TYPE_MOB
            else -> throw IllegalStateException("Unknown tile item type - ${tileItem.type}")
        }
    }

    override fun reset() {
        isActive = false
        sendEvent(TriggerCanvasService.ResetSelectedArea())
    }

    override fun destroy() {
        reset()
        tileItemTypeToDelete = null
    }

    private fun fillAreaRect(x: Int, y: Int) {
        x1 = min(xStart, x)
        y1 = min(yStart, y)
        x2 = max(xStart, x)
        y2 = max(yStart, y)
        sendEvent(TriggerCanvasService.SelectArea(MapArea(x1, y1, x2, y2)))
    }
}
