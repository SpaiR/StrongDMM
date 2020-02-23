package strongdmm.controller.tool

import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.event.Event
import strongdmm.event.EventSender

class TileDeleteTool : Tool(), EventSender {
    private val dirtyTiles: MutableSet<MapPos> = mutableSetOf()
    private val reverseActions: MutableList<Undoable> = mutableListOf()

    private var tileItemTypeToDelete: String? = null
    private var currentMap: Dmm? = null

    override fun onStart(mapPos: MapPos) {
        isActive = currentMap != null && tileItemTypeToDelete != null

        if (isActive && dirtyTiles.add(mapPos)) {
            deleteTopmostTileItem(mapPos)
        }
    }

    override fun onStop() {
        isActive = false
        dirtyTiles.clear()
        flushReverseActions()
        sendEvent(Event.CanvasController.ResetSelectedTiles())
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

    override fun onMapSwitch(map: Dmm?) {
        currentMap = map
    }

    private fun deleteTopmostTileItem(pos: MapPos) {
        currentMap?.getTile(pos.x, pos.y)?.let { tile ->
            sendEvent(Event.LayersFilterController.Fetch { filteredTypes ->
                tile.getFilteredTileItems(filteredTypes).findLast { it.isType(tileItemTypeToDelete!!) }?.let { tileItem ->
                    reverseActions.add(ReplaceTileAction(tile) {
                        tile.deleteTileItem(tileItem)
                    })

                    sendEvent(Event.CanvasController.SelectTiles(dirtyTiles))
                    sendEvent(Event.Global.RefreshFrame())
                }
            })
        }
    }

    private fun flushReverseActions() {
        if (reverseActions.isEmpty()) {
            return
        }

        sendEvent(Event.ActionController.AddAction(MultiAction(reverseActions.toList())))
        reverseActions.clear()
    }
}
