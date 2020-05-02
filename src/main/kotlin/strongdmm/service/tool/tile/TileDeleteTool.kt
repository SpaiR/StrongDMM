package strongdmm.service.tool.tile

import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.service.*
import strongdmm.service.action.undoable.MultiAction
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.service.action.undoable.Undoable
import strongdmm.service.tool.Tool

class TileDeleteTool : Tool(), EventHandler {
    private val dirtyTiles: MutableSet<MapPos> = mutableSetOf()
    private val reverseActions: MutableList<Undoable> = mutableListOf()

    private var tileItemTypeToDelete: String? = null

    override fun onStart(mapPos: MapPos) {
        isActive = tileItemTypeToDelete != null

        if (isActive && dirtyTiles.add(mapPos)) {
            deleteTopmostTileItem(mapPos)
        }
    }

    override fun onStop() {
        flushReverseActions()
        reset()
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        if (dirtyTiles.add(mapPos)) {
            deleteTopmostTileItem(mapPos)
        }
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
        dirtyTiles.clear()
        reverseActions.clear()
        sendEvent(TriggerCanvasService.ResetSelectedTiles())
    }

    override fun destroy() {
        reset()
        tileItemTypeToDelete = null
    }

    private fun deleteTopmostTileItem(pos: MapPos) {
        sendEvent(TriggerMapHolderService.FetchSelectedMap { selectedMap ->
            val tile = selectedMap.getTile(pos.x, pos.y, selectedMap.zSelected)

            sendEvent(TriggerLayersFilterService.FetchFilteredLayers { filteredTypes ->
                tile.getFilteredTileItems(filteredTypes).findLast { it.isType(tileItemTypeToDelete!!) }?.let { tileItem ->
                    reverseActions.add(ReplaceTileAction(tile) {
                        tile.deleteTileItem(tileItem)
                    })

                    sendEvent(TriggerCanvasService.SelectTiles(dirtyTiles))
                    sendEvent(TriggerFrameService.RefreshFrame())
                }
            })
        })
    }

    private fun flushReverseActions() {
        if (reverseActions.isEmpty()) {
            return
        }

        sendEvent(TriggerActionService.AddAction(MultiAction(reverseActions.toList())))
    }
}
