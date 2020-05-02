package strongdmm.controller.tool.tile

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.controller.tool.Tool
import strongdmm.event.EventSender
import strongdmm.event.type.controller.TriggerActionController
import strongdmm.event.type.controller.TriggerCanvasController
import strongdmm.event.type.controller.TriggerFrameController
import strongdmm.event.type.controller.TriggerMapHolderController

class TileAddTool : Tool(), EventSender {
    private val dirtyTiles: MutableSet<MapPos> = mutableSetOf()
    private val reverseActions: MutableList<Undoable> = mutableListOf()

    private var selectedTileItem: TileItem? = null

    override fun onStart(mapPos: MapPos) {
        isActive = selectedTileItem != null

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
        selectedTileItem = tileItem
    }

    override fun reset() {
        isActive = false
        dirtyTiles.clear()
        reverseActions.clear()
        sendEvent(TriggerCanvasController.ResetSelectedTiles())
    }

    override fun destroy() {
        reset()
        selectedTileItem = null
    }

    private fun addTileItem(pos: MapPos) {
        sendEvent(TriggerMapHolderController.FetchSelectedMap { selectedMap ->
            val tile = selectedMap.getTile(pos.x, pos.y, selectedMap.zSelected)

            reverseActions.add(ReplaceTileAction(tile) {
                tile.addTileItem(selectedTileItem!!)
            })

            sendEvent(TriggerCanvasController.SelectTiles(dirtyTiles))
            sendEvent(TriggerFrameController.RefreshFrame())
        })
    }

    private fun flushReverseActions() {
        if (reverseActions.isEmpty()) {
            return
        }

        sendEvent(TriggerActionController.AddAction(MultiAction(reverseActions.toList())))
    }
}
