package strongdmm.ui.search

import imgui.ImBool
import imgui.ImGui.*
import imgui.enums.ImGuiCond
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapId
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.imgui.button
import strongdmm.util.imgui.setItemHoveredTooltip
import strongdmm.util.imgui.window

class SearchResultPanelUi : EventConsumer, EventSender {
    private val searchResults: MutableSet<SearchResult> = mutableSetOf()
    private val panelsOpenState: MutableMap<String, ImBool> = mutableMapOf()
    private val searchResultsToRemove: MutableList<SearchResult> = mutableListOf()

    private var currentMapId: MapId = MapId.NONE

    init {
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
        consumeEvent(Event.SearchResultPanelUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (searchResults.isEmpty()) {
            return
        }

        searchResultsToRemove.clear()

        searchResults.forEach { searchResult ->
            setNextWindowPos(655f, 535f, ImGuiCond.Once)
            setNextWindowSize(350f, 390f, ImGuiCond.Once)

            val openState = panelsOpenState.getOrPut(searchResult.type) { ImBool(true) }

            if (openState.get()) {
                window("Search Result: ${searchResult.type} (${searchResult.positions.size})", openState) {
                    columns(getWindowWidth().toInt() / 100, "search_result_columns", false)
                    searchResult.positions.forEachIndexed { idx, searchPos ->
                        button("x:%03d y:%03d##jump_btn_$idx".format(searchPos.pos.x, searchPos.pos.y)) {
                            sendEvent(Event.CanvasController.CenterPosition(searchPos.pos))
                            sendEvent(Event.CanvasController.MarkPosition(searchPos.pos))
                        }
                        setItemHoveredTooltip("[%d] %s".format(searchPos.idx, searchPos.tileItemType))
                        nextColumn()
                    }
                }
            } else {
                sendEvent(Event.CanvasController.ResetMarkedPosition())
                panelsOpenState.remove(searchResult.type)
                searchResultsToRemove.add(searchResult)
            }
        }

        searchResults.removeAll(searchResultsToRemove)
    }

    private fun clearAll() {
        searchResults.clear()
        panelsOpenState.clear()
    }

    private fun handleResetEnvironment() {
        clearAll()
    }

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        currentMapId = event.body.id
        clearAll()
    }

    private fun handleCloseMap(event: Event<Dmm, Unit>) {
        if (event.body.id == currentMapId) {
            currentMapId = MapId.NONE
            clearAll()
        }
    }

    private fun handleOpen(event: Event<SearchResult, Unit>) {
        searchResults.remove(event.body)
        searchResults.add(event.body)
    }
}
