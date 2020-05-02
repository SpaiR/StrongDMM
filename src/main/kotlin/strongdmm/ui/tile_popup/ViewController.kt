package strongdmm.ui.tile_popup

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.*
import strongdmm.event.type.ui.TriggerEditVarsDialogUi
import strongdmm.service.action.undoable.ReplaceTileAction

class ViewController(
    private val state: State
) : EventHandler {
    private val nudgeFlow = ViewControllerNudgeFlow(state)
    private val dirFlow = ViewControllerDirFlow(state)

    fun doUndo() {
        sendEvent(TriggerActionService.UndoAction())
    }

    fun doRedo() {
        sendEvent(TriggerActionService.RedoAction())
    }

    fun doCut() {
        sendEvent(TriggerClipboardService.Cut())
    }

    fun doCopy() {
        sendEvent(TriggerClipboardService.Copy())
    }

    fun doPaste() {
        sendEvent(TriggerClipboardService.Paste())
    }

    fun doDelete() {
        sendEvent(TriggerMapModifierService.DeleteTileItemsInSelectedArea())
    }

    fun doDeselectAll() {
        sendEvent(TriggerToolsService.ResetTool())
    }

    fun doMoveToTop(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        sendEvent(
            TriggerActionService.AddAction(ReplaceTileAction(tile) {
                tile.moveToTop(tileItem, tileItemIdx)
            })
        )

        sendEvent(TriggerFrameService.RefreshFrame())
    }

    fun doMoveToBottom(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        sendEvent(
            TriggerActionService.AddAction(ReplaceTileAction(tile) {
                tile.moveToBottom(tileItem, tileItemIdx)
            })
        )

        sendEvent(TriggerFrameService.RefreshFrame())
    }

    fun doMakeActiveObject(tileItem: TileItem) {
        sendEvent(TriggerTileItemService.ChangeSelectedTileItem(tileItem))
    }

    fun doEdit(tile: Tile, tileItemIdx: Int) {
        sendEvent(TriggerEditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
    }

    fun doDeleteObject(tile: Tile, tileItemIdx: Int) {
        sendEvent(
            TriggerActionService.AddAction(ReplaceTileAction(tile) {
                tile.deleteTileItem(tileItemIdx)
            })
        )

        sendEvent(TriggerFrameService.RefreshFrame())
    }

    fun doReplaceWithSelectedTileItem(tile: Tile, tileItemIdx: Int) {
        state.selectedTileItem?.let { selectedTileItem ->
            sendEvent(
                TriggerActionService.AddAction(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItemIdx, selectedTileItem)
                })
            )

            sendEvent(TriggerFrameService.RefreshFrame())
        }
    }

    fun doResetToDefault(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        sendEvent(
            TriggerActionService.AddAction(ReplaceTileAction(tile) {
                tile.replaceTileItem(tileItemIdx, GlobalTileItemHolder.getOrCreate(tileItem.type))
            })
        )

        sendEvent(TriggerFrameService.RefreshFrame())
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
