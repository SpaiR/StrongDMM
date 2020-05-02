package strongdmm.ui.tile_popup

import strongdmm.byond.dirToRel
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.byond.relToDir
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerActionController
import strongdmm.event.type.controller.TriggerFrameController
import strongdmm.event.type.ui.TriggerObjectPanelUi
import strongdmm.util.extension.getOrPut

class ViewControllerDirFlow(
    private val state: State
) : EventHandler {
    fun doDir(tile: Tile, tileItem: TileItem, tileItemIdx: Int, relativeDir: IntArray) {
        GlobalTileItemHolder.tmpOperation {
            tile.setDir(tileItem, tileItemIdx, relToDir(relativeDir[0]))
        }

        sendEvent(TriggerFrameController.RefreshFrame())
    }

    fun getDirValueToShow(tileItem: TileItem, tileItemIdx: Int): Pair<Int, IntArray> {
        return state.dirArrays.getOrPut(tileItemIdx) { tileItem.dir to intArrayOf(dirToRel(tileItem.dir)) }
    }

    fun applyDirChanges(tile: Tile, tileItem: TileItem, tileItemIdx: Int, initialValue: Int, relativeDir: IntArray) {
        GlobalTileItemHolder.tmpOperation {
            tile.setDir(tileItem, tileItemIdx, initialValue)
        }

        sendEvent(
            TriggerActionController.AddAction(ReplaceTileAction(tile) {
                tile.setDir(tileItem, tileItemIdx, relToDir(relativeDir[0]))
            })
        )

        sendEvent(TriggerFrameController.RefreshFrame())
        sendEvent(TriggerObjectPanelUi.Update())

        state.dirArrays.clear() // to properly create a reverse action
    }
}
