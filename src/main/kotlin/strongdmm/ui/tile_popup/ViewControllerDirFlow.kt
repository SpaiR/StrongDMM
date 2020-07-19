package strongdmm.ui.tile_popup

import strongdmm.byond.dirToRel
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.byond.relToDir
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerActionService
import strongdmm.event.type.service.TriggerFrameService
import strongdmm.event.type.ui.TriggerObjectPanelUi
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.util.extension.getOrPut

class ViewControllerDirFlow(
    private val state: State
) : EventHandler {
    fun doDir(tile: Tile, tileItem: TileItem, tileItemIdx: Int, relativeDir: IntArray) {
        GlobalTileItemHolder.tmpOperation {
            tile.setDir(tileItem, tileItemIdx, relToDir(relativeDir[0]))
        }

        sendEvent(TriggerFrameService.RefreshFrame())
    }

    fun getDirValueToShow(tileItem: TileItem, tileItemIdx: Int): Pair<Int, IntArray> {
        return state.dirArrays.getOrPut(tileItemIdx) { tileItem.dir to intArrayOf(dirToRel(tileItem.dir)) }
    }

    fun applyDirChanges(tile: Tile, tileItem: TileItem, tileItemIdx: Int, initialValue: Int, relativeDir: IntArray) {
        GlobalTileItemHolder.tmpOperation {
            tile.setDir(tileItem, tileItemIdx, initialValue)
        }

        sendEvent(
            TriggerActionService.QueueUndoable(ReplaceTileAction(tile) {
                tile.setDir(tileItem, tileItemIdx, relToDir(relativeDir[0]))
            })
        )

        sendEvent(TriggerFrameService.RefreshFrame())
        sendEvent(TriggerObjectPanelUi.Update())

        state.dirArrays.clear() // to properly create a reverse action
    }
}
