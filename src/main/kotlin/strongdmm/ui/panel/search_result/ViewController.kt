package strongdmm.ui.panel.search_result

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerCanvasService
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.event.type.service.TriggerMapModifierService
import strongdmm.ui.panel.search_result.model.SearchPosition

class ViewController(
    private val state: State
) : EventHandler {
    fun doDelete(searchPosition: SearchPosition) {
        delete(listOf(Pair(searchPosition.tileItem, searchPosition.pos)))
        state.positionsToRemove.add(searchPosition)
    }

    fun doDeleteAll() {
        delete(state.searchResult!!.positions.asSequence().map { Pair(it.tileItem, it.pos) }.toList())
        dispose()
    }

    fun doReplace(searchPosition: SearchPosition) {
        if (replace(listOf(Pair(searchPosition.tileItem, searchPosition.pos)))) {
            state.positionsToRemove.add(searchPosition)
        }
    }

    fun doReplaceAll() {
        if (replace(state.searchResult!!.positions.asSequence().map { Pair(it.tileItem, it.pos) }.toList())) {
            dispose()
        }
    }

    fun doJump(searchPosition: SearchPosition) {
        sendEvent(TriggerMapHolderService.ChangeSelectedZ(searchPosition.pos.z))
        sendEvent(TriggerCanvasService.CenterCanvasByPosition(searchPosition.pos))
        sendEvent(TriggerCanvasService.MarkPosition(searchPosition.pos))
    }

    fun dispose() {
        sendEvent(TriggerCanvasService.ResetMarkedPosition())
        state.isOpen.set(false)
        state.searchResult = null
    }

    private fun delete(deletionList: List<Pair<TileItem, MapPos>>) {
        if (state.searchResult!!.isSearchById) {
            sendEvent(TriggerMapModifierService.DeleteTileItemsWithIdInPositions(deletionList))
        } else {
            sendEvent(TriggerMapModifierService.DeleteTileItemsWithTypeInPositions(deletionList))
        }

        sendEvent(TriggerCanvasService.ResetMarkedPosition())
    }

    private fun replace(replaceList: List<Pair<TileItem, MapPos>>): Boolean {
        if (!state.isReplaceEnabled) {
            return false
        }

        if (state.searchResult!!.isSearchById) {
            sendEvent(TriggerMapModifierService.ReplaceTileItemsWithIdInPositions(Pair(state.replaceType.get(), replaceList)))
        } else {
            sendEvent(TriggerMapModifierService.ReplaceTileItemsWithTypeInPositions(Pair(state.replaceType.get(), replaceList)))
        }

        sendEvent(TriggerCanvasService.ResetMarkedPosition())
        return true
    }

    fun checkReplaceModeEnabled() {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            state.isReplaceEnabled = it.getItem(state.replaceType.get()) != null
        })
    }

    fun checkSearchPositionsToRemove() {
        if (state.positionsToRemove.isNotEmpty()) {
            state.searchResult?.positions?.removeAll(state.positionsToRemove)
            state.positionsToRemove.clear()
        }
    }
}
