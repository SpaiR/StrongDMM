package strongdmm.ui.tile_popup

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.service.preferences.NudgeMode
import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerActionController
import strongdmm.event.type.controller.TriggerFrameController
import strongdmm.event.type.ui.TriggerObjectPanelUi
import strongdmm.util.extension.getOrPut

class ViewControllerNudgeFlow(
    private val state: State
) : EventHandler {
    fun doNudge(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int, pixelNudge: IntArray) {
        GlobalTileItemHolder.tmpOperation {
            tile.nudge(isXAxis, tileItem, tileItemIdx, pixelNudge[0], state.providedPreferences.nudgeMode)
        }

        sendEvent(TriggerFrameController.RefreshFrame())
    }

    fun getNudgeValueToShow(isXAxis: Boolean, tileItem: TileItem, tileItemIdx: Int): Pair<Int, IntArray> {
        return when (state.providedPreferences.nudgeMode) {
            NudgeMode.PIXEL -> {
                if (isXAxis) {
                    state.pixelXNudgeArrays.getOrPut(tileItemIdx) { tileItem.pixelX to intArrayOf(tileItem.pixelX) }
                } else {
                    state.pixelYNudgeArrays.getOrPut(tileItemIdx) { tileItem.pixelY to intArrayOf(tileItem.pixelY) }
                }
            }
            NudgeMode.STEP -> {
                if (isXAxis) {
                    state.pixelXNudgeArrays.getOrPut(tileItemIdx) { tileItem.stepX to intArrayOf(tileItem.stepX) }
                } else {
                    state.pixelYNudgeArrays.getOrPut(tileItemIdx) { tileItem.stepY to intArrayOf(tileItem.stepY) }
                }
            }
        }
    }

    fun applyNudgeChanges(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int, pixelNudge: IntArray, initialValue: Int) {
        GlobalTileItemHolder.tmpOperation {
            tile.nudge(isXAxis, tileItem, tileItemIdx, initialValue, state.providedPreferences.nudgeMode)
        }

        sendEvent(
            TriggerActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.nudge(isXAxis, tileItem, tileItemIdx, pixelNudge[0], state.providedPreferences.nudgeMode)
                }
            ))

        sendEvent(TriggerFrameController.RefreshFrame())
        sendEvent(TriggerObjectPanelUi.Update())

        // to properly create a reverse action
        if (isXAxis) {
            state.pixelXNudgeArrays.clear()
        } else {
            state.pixelYNudgeArrays.clear()
        }
    }
}
