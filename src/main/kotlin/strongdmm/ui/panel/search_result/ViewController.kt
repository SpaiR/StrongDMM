package strongdmm.ui.panel.search_result

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventBus
import strongdmm.event.type.service.TriggerCanvasService
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.event.type.service.TriggerMapModifierService
import strongdmm.ui.panel.search_result.model.SearchPosition

class ViewController(
    private val state: State
) {
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
        EventBus.post(TriggerMapHolderService.ChangeSelectedZ(searchPosition.pos.z))
        EventBus.post(TriggerCanvasService.CenterCanvasByPosition(searchPosition.pos))
        EventBus.post(TriggerCanvasService.MarkPosition(searchPosition.pos))
    }

    fun dispose() {
        EventBus.post(TriggerCanvasService.ResetMarkedPosition())
        state.isOpen.set(false)
        state.searchResult = null
    }

    private fun delete(deletionList: List<Pair<TileItem, MapPos>>) {
        if (state.searchResult!!.isSearchById) {
            EventBus.post(TriggerMapModifierService.DeleteTileItemsWithIdInPositions(deletionList))
        } else {
            EventBus.post(TriggerMapModifierService.DeleteTileItemsWithTypeInPositions(deletionList))
        }

        EventBus.post(TriggerCanvasService.ResetMarkedPosition())
    }

    private fun replace(replaceList: List<Pair<TileItem, MapPos>>): Boolean {
        if (!state.isReplaceEnabled) {
            return false
        }

        if (state.searchResult!!.isSearchById) {
            EventBus.post(TriggerMapModifierService.ReplaceTileItemsWithIdInPositions(Pair(state.replaceType.get(), replaceList)))
        } else {
            EventBus.post(TriggerMapModifierService.ReplaceTileItemsWithTypeInPositions(Pair(state.replaceType.get(), replaceList)))
        }

        EventBus.post(TriggerCanvasService.ResetMarkedPosition())
        return true
    }

    fun checkReplaceModeEnabled() {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment {
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
