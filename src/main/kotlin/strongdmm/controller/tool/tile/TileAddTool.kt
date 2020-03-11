package strongdmm.controller.tool.tile

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.controller.tool.Tool
import strongdmm.event.Event
import strongdmm.event.EventSender
import strongdmm.event.type.EventFrameController

class TileAddTool : Tool(), EventSender {
    private val dirtyTiles: MutableSet<MapPos> = mutableSetOf()
    private val reverseActions: MutableList<Undoable> = mutableListOf()

    private var activeTileItem: TileItem? = null

    override fun onStart(mapPos: MapPos) {
        isActive = activeTileItem != null

        if (isActive && dirtyTiles.add(mapPos)) {
            addTileItem(mapPos)
        }
    }

    override fun onStop() {
        flushReverseActions()
        reset()
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        if (dirtyTiles.add(mapPos)) {
            addTileItem(mapPos)
        }
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        activeTileItem = tileItem
    }

    override fun reset() {
        isActive = false
        dirtyTiles.clear()
        reverseActions.clear()
        sendEvent(Event.CanvasController.ResetSelectedTiles())
    }

    override fun destroy() {
        reset()
        activeTileItem = null
    }

    private fun addTileItem(pos: MapPos) {
        sendEvent(Event.MapHolderController.FetchSelected { selectedMap ->
            val tile = selectedMap.getTile(pos.x, pos.y)

            reverseActions.add(ReplaceTileAction(tile) {
                tile.addTileItem(activeTileItem!!)
            })

            sendEvent(Event.CanvasController.SelectTiles(dirtyTiles))
            sendEvent(EventFrameController.Refresh())
        })
    }

    private fun flushReverseActions() {
        if (reverseActions.isEmpty()) {
            return
        }

        sendEvent(Event.ActionController.AddAction(MultiAction(reverseActions.toList())))
    }
}
