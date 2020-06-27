package strongdmm.ui.tile_popup

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerActionService
import strongdmm.event.type.service.TriggerFrameService
import strongdmm.event.type.ui.TriggerObjectPanelUi
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.service.preferences.prefs.enums.NudgeMode
import strongdmm.util.extension.getOrPut

class ViewControllerNudgeFlow(
    private val state: State
) : EventHandler {
    fun doNudge(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int, pixelNudge: IntArray) {
        GlobalTileItemHolder.tmpOperation {
            tile.nudge(isXAxis, tileItem, tileItemIdx, pixelNudge[0], state.providedPreferences.nudgeMode)
        }

        sendEvent(TriggerFrameService.RefreshFrame())
    }

    fun getNudgeValueToShow(isXAxis: Boolean, tileItem: TileItem, tileItemIdx: Int): Pair<Int, IntArray> {
        return if (state.providedPreferences.nudgeMode == NudgeMode.PIXEL) {
            if (isXAxis) {
                state.pixelXNudgeArrays.getOrPut(tileItemIdx) { tileItem.pixelX to intArrayOf(tileItem.pixelX) }
            } else {
                state.pixelYNudgeArrays.getOrPut(tileItemIdx) { tileItem.pixelY to intArrayOf(tileItem.pixelY) }
            }
        } else {
            if (isXAxis) {
                state.pixelXNudgeArrays.getOrPut(tileItemIdx) { tileItem.stepX to intArrayOf(tileItem.stepX) }
            } else {
                state.pixelYNudgeArrays.getOrPut(tileItemIdx) { tileItem.stepY to intArrayOf(tileItem.stepY) }
            }
        }
    }

    fun applyNudgeChanges(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int, pixelNudge: IntArray, initialValue: Int) {
        GlobalTileItemHolder.tmpOperation {
            tile.nudge(isXAxis, tileItem, tileItemIdx, initialValue, state.providedPreferences.nudgeMode)
        }

        sendEvent(
            TriggerActionService.AddAction(
                ReplaceTileAction(tile) {
                    tile.nudge(isXAxis, tileItem, tileItemIdx, pixelNudge[0], state.providedPreferences.nudgeMode)
                }
            ))

        sendEvent(TriggerFrameService.RefreshFrame())
        sendEvent(TriggerObjectPanelUi.Update())

        // to properly create a reverse action
        if (isXAxis) {
            state.pixelXNudgeArrays.clear()
        } else {
            state.pixelYNudgeArrays.clear()
        }
    }
}
