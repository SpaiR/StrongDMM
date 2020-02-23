package strongdmm.controller.tool

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.event.Event
import strongdmm.event.EventSender

class TileAddTool : Tool(), EventSender {
    private val dirtyTiles: MutableSet<MapPos> = mutableSetOf()
    private val reverseActions: MutableList<Undoable> = mutableListOf()

    private var usedTileItem: TileItem? = null
    private var currentMap: Dmm? = null

    override fun onStart(mapPos: MapPos) {
        isActive = currentMap != null && usedTileItem != null

        if (isActive && dirtyTiles.add(mapPos)) {
            addTileItem(mapPos)
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
            addTileItem(mapPos)
        }
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        usedTileItem = tileItem
    }

    override fun onMapSwitch(map: Dmm?) {
        currentMap = map
    }

    private fun addTileItem(pos: MapPos) {
        currentMap?.getTile(pos.x, pos.y)?.let { tile ->
            reverseActions.add(ReplaceTileAction(tile) {
                tile.addTileItem(usedTileItem!!)
            })

            sendEvent(Event.CanvasController.SelectTiles(dirtyTiles))
            sendEvent(Event.Global.RefreshFrame())
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
