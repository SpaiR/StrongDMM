package strongdmm.ui.panel.search_result

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerCanvasController
import strongdmm.event.type.controller.TriggerEnvironmentController
import strongdmm.event.type.controller.TriggerMapModifierController
import strongdmm.ui.panel.search_result.model.SearchPosition

class ViewController(
    private val state: State
) : EventHandler {
    fun doDelete(searchPosition: SearchPosition) {
        delete(mutableListOf(Pair(searchPosition.tileItem, searchPosition.pos)))
    }

    fun doDeleteAll() {
        delete(state.searchResult!!.positions.asSequence().map { Pair(it.tileItem, it.pos) }.toList())
        dispose()
    }

    fun doReplace(searchPosition: SearchPosition) {
        replace(mutableListOf(Pair(searchPosition.tileItem, searchPosition.pos)))
    }

    fun doReplaceAll() {
        replace(state.searchResult!!.positions.asSequence().map { Pair(it.tileItem, it.pos) }.toList())
        dispose()
    }

    fun doJump(searchPosition: SearchPosition) {
        sendEvent(TriggerCanvasController.CenterCanvasByPosition(searchPosition.pos))
        sendEvent(TriggerCanvasController.MarkPosition(searchPosition.pos))
    }

    fun dispose() {
        state.isOpen.set(false)
        state.searchResult = null
    }

    private fun delete(deletionList: List<Pair<TileItem, MapPos>>) {
        if (state.searchResult!!.isSearchById) {
            sendEvent(TriggerMapModifierController.DeleteTileItemsWithIdInPositions(deletionList))
        } else {
            sendEvent(TriggerMapModifierController.DeleteTileItemsWithTypeInPositions(deletionList))
        }

        sendEvent(TriggerCanvasController.ResetMarkedPosition())
    }

    private fun replace(replaceList: List<Pair<TileItem, MapPos>>) {
        if (!state.isReplaceEnabled) {
            return
        }

        if (state.searchResult!!.isSearchById) {
            sendEvent(TriggerMapModifierController.ReplaceTileItemsWithIdInPositions(Pair(state.replaceType.get(), replaceList)))
        } else {
            sendEvent(TriggerMapModifierController.ReplaceTileItemsWithTypeInPositions(Pair(state.replaceType.get(), replaceList)))
        }

        sendEvent(TriggerCanvasController.ResetMarkedPosition())
    }

    fun checkReplaceModeEnabled() {
        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment {
            state.isReplaceEnabled = it.getItem(state.replaceType.get()) != null
        })
    }
}
