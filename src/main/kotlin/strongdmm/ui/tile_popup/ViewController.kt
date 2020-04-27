package strongdmm.ui.tile_popup

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.*
import strongdmm.event.type.ui.TriggerEditVarsDialogUi

class ViewController(
    private val state: State
) : EventHandler {
    private val nudgeFlow = ViewControllerNudgeFlow(state)
    private val dirFlow = ViewControllerDirFlow(state)

    fun doUndo() {
        sendEvent(TriggerActionController.UndoAction())
    }

    fun doRedo() {
        sendEvent(TriggerActionController.RedoAction())
    }

    fun doCut() {
        sendEvent(TriggerClipboardController.Cut())
    }

    fun doCopy() {
        sendEvent(TriggerClipboardController.Copy())
    }

    fun doPaste() {
        sendEvent(TriggerClipboardController.Paste())
    }

    fun doDelete() {
        sendEvent(TriggerMapModifierController.DeleteTileItemsInActiveArea())
    }

    fun doDeselectAll() {
        sendEvent(TriggerToolsController.ResetTool())
    }

    fun doMoveToTop(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        sendEvent(
            TriggerActionController.AddAction(ReplaceTileAction(tile) {
                tile.moveToTop(tileItem, tileItemIdx)
            })
        )

        sendEvent(TriggerFrameController.RefreshFrame())
    }
    fun doMoveToBottom(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        sendEvent(
            TriggerActionController.AddAction(ReplaceTileAction(tile) {
                tile.moveToBottom(tileItem, tileItemIdx)
            })
        )

        sendEvent(TriggerFrameController.RefreshFrame())
    }

    fun doMakeActiveObject(tileItem: TileItem) {
        sendEvent(TriggerTileItemController.ChangeSelectedTileItem(tileItem))
    }

    fun doEdit(tile: Tile, tileItemIdx: Int) {
        sendEvent(TriggerEditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
    }

    fun doDeleteObject(tile: Tile, tileItemIdx: Int) {
        sendEvent(
            TriggerActionController.AddAction(ReplaceTileAction(tile) {
                tile.deleteTileItem(tileItemIdx)
            })
        )

        sendEvent(TriggerFrameController.RefreshFrame())
    }

    fun doReplaceWithActiveObject(tile: Tile, tileItemIdx: Int) {
        state.selectedTileItem?.let { activeTileItem ->
            sendEvent(
                TriggerActionController.AddAction(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItemIdx, activeTileItem)
                })
            )

            sendEvent(TriggerFrameController.RefreshFrame())
        }
    }

    fun doResetToDefault(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        sendEvent(
            TriggerActionController.AddAction(ReplaceTileAction(tile) {
                tile.replaceTileItem(tileItemIdx, GlobalTileItemHolder.getOrCreate(tileItem.type))
            })
        )

        sendEvent(TriggerFrameController.RefreshFrame())
    }

    fun doNudge(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int, pixelNudge: IntArray) {
        nudgeFlow.doNudge(isXAxis, tile, tileItem, tileItemIdx, pixelNudge)
    }

    fun doDir(tile: Tile, tileItem: TileItem, tileItemIdx: Int, relativeDir: IntArray) {
        dirFlow.doDir(tile, tileItem, tileItemIdx, relativeDir)
    }

    fun isSameTypeAsSelectedObject(tileItem: TileItem): Boolean {
        return state.selectedTileItem?.isSameType(tileItem) ?: false
    }

    fun getNudgeValueToShow(isXAxis: Boolean, tileItem: TileItem, tileItemIdx: Int): Pair<Int, IntArray> {
        return nudgeFlow.getNudgeValueToShow(isXAxis, tileItem, tileItemIdx)
    }

    fun applyNudgeChanges(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int, pixelNudge: IntArray, initialValue: Int) {
        nudgeFlow.applyNudgeChanges(isXAxis, tile, tileItem, tileItemIdx, pixelNudge, initialValue)
    }

    fun getDirValueToShow(tileItem: TileItem, tileItemIdx: Int): Pair<Int, IntArray> {
        return dirFlow.getDirValueToShow(tileItem, tileItemIdx)
    }

    fun applyDirChanges(tile: Tile, tileItem: TileItem, tileItemIdx: Int, initialValue: Int, relativeDir: IntArray) {
        dirFlow.applyDirChanges(tile, tileItem, tileItemIdx, initialValue, relativeDir)
    }

    fun dispose() {
        state.currentTile = null
        state.pixelXNudgeArrays.clear()
        state.pixelYNudgeArrays.clear()
        state.dirArrays.clear()
        sendEvent(Reaction.TilePopupClosed())
    }
}
