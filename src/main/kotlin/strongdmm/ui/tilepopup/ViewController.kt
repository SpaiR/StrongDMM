package strongdmm.ui.tilepopup

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventBus
import strongdmm.event.type.service.*
import strongdmm.event.type.ui.ReactionTilePopupUi
import strongdmm.event.type.ui.TriggerEditVarsDialogUi
import strongdmm.service.action.undoable.ReplaceTileAction

class ViewController(
    private val state: State
) {
    private val nudgeFlow = ViewControllerNudgeFlow(state)
    private val dirFlow = ViewControllerDirFlow(state)

    fun doUndo() {
        EventBus.post(TriggerActionService.UndoAction())
    }

    fun doRedo() {
        EventBus.post(TriggerActionService.RedoAction())
    }

    fun doCut() {
        EventBus.post(TriggerClipboardService.Cut())
    }

    fun doCopy() {
        EventBus.post(TriggerClipboardService.Copy())
    }

    fun doPaste() {
        EventBus.post(TriggerClipboardService.Paste())
    }

    fun doDelete() {
        EventBus.post(TriggerMapModifierService.DeleteTileItemsInSelectedArea())
    }

    fun doDeselectAll() {
        EventBus.post(TriggerToolsService.ResetTool())
    }

    fun doMoveToTop(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        EventBus.post(
            TriggerActionService.QueueUndoable(ReplaceTileAction(tile) {
                tile.moveToTop(tileItem, tileItemIdx)
            })
        )

        EventBus.post(TriggerFrameService.RefreshFrame())
    }

    fun doMoveToBottom(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        EventBus.post(
            TriggerActionService.QueueUndoable(ReplaceTileAction(tile) {
                tile.moveToBottom(tileItem, tileItemIdx)
            })
        )

        EventBus.post(TriggerFrameService.RefreshFrame())
    }

    fun doMakeActiveObject(tileItem: TileItem) {
        EventBus.post(TriggerTileItemService.ChangeSelectedTileItem(tileItem))
    }

    fun doEdit(tile: Tile, tileItemIdx: Int) {
        EventBus.post(TriggerEditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
    }

    fun doDeleteObject(tile: Tile, tileItemIdx: Int) {
        EventBus.post(
            TriggerActionService.QueueUndoable(ReplaceTileAction(tile) {
                tile.deleteTileItem(tileItemIdx)
            })
        )

        EventBus.post(TriggerFrameService.RefreshFrame())
    }

    fun doReplaceWithSelectedTileItem(tile: Tile, tileItemIdx: Int) {
        state.selectedTileItem?.let { selectedTileItem ->
            EventBus.post(
                TriggerActionService.QueueUndoable(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItemIdx, selectedTileItem)
                })
            )

            EventBus.post(TriggerFrameService.RefreshFrame())
        }
    }

    fun doResetToDefault(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        EventBus.post(
            TriggerActionService.QueueUndoable(ReplaceTileAction(tile) {
                tile.replaceTileItem(tileItemIdx, GlobalTileItemHolder.getOrCreate(tileItem.type))
            })
        )

        EventBus.post(TriggerFrameService.RefreshFrame())
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
        if (state.isDisposed) {
            return
        }

        state.currentTile = null
        state.pixelXNudgeArrays.clear()
        state.pixelYNudgeArrays.clear()
        state.dirArrays.clear()
        EventBus.post(ReactionTilePopupUi.TilePopupClosed.SIGNAL)

        state.isDisposed = true
    }
}
