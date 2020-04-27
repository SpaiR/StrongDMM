package strongdmm.ui.panel.instance_locator

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerInstanceController
import strongdmm.event.type.controller.TriggerToolsController
import strongdmm.event.type.ui.TriggerSearchResultPanelUi
import strongdmm.ui.search.SearchRect
import strongdmm.ui.search.SearchResult

class ViewController(
    private val state: State
) : EventHandler {
    fun doSearch() {
        val type = state.searchType.get().trim()

        if (type.isEmpty()) {
            return
        }

        val tileItemId = type.toLongOrNull()
        val searchRect = SearchRect(state.searchX1.get(), state.searchY1.get(), state.searchX2.get(), state.searchY2.get())

        val openSearchResult = { it: List<Pair<TileItem, MapPos>> ->
            if (it.isNotEmpty()) {
                sendEvent(TriggerSearchResultPanelUi.Open(SearchResult(type, tileItemId != null, it)))
            }
        }

        if (tileItemId != null) {
            sendEvent(TriggerInstanceController.FindInstancePositionsById(Pair(searchRect, tileItemId), openSearchResult))
        } else {
            sendEvent(TriggerInstanceController.FindInstancePositionsByType(Pair(searchRect, type), openSearchResult))
        }
    }

    fun doSelection() {
        sendEvent(TriggerToolsController.FetchActiveArea { activeArea ->
            state.searchX1.set(activeArea.x1)
            state.searchY1.set(activeArea.y1)
            state.searchX2.set(activeArea.x2)
            state.searchY2.set(activeArea.y2)
        })
    }

    fun doReset() {
        state.searchX1.set(1)
        state.searchY1.set(1)
        state.searchX2.set(state.mapMaxX)
        state.searchY2.set(state.mapMaxY)
    }
}
