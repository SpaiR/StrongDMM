package strongdmm.ui.search

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiCond
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.RMB
import strongdmm.util.imgui.*

class SearchResultPanelUi : EventConsumer, EventSender {
    private val searchResults: MutableSet<SearchResult> = mutableSetOf()
    private val panelsOpenState: MutableMap<String, ImBool> = mutableMapOf()

    private var currentMapId: Int = Dmm.MAP_ID_NONE

    private val replaceType: ImString = ImString(50).apply { inputData.isResizable = true }
    private var isReplaceEnabled: Boolean = false

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

        val searchResIterator = searchResults.iterator()
        while (searchResIterator.hasNext()) {
            val searchResult = searchResIterator.next()

            if (searchResult.positions.isEmpty()) {
                searchResIterator.remove()
                continue
            }

            setNextWindowPos(655f, 535f, ImGuiCond.Once)
            setNextWindowSize(375f, 390f, ImGuiCond.Once)

            val openState = panelsOpenState.getOrPut(searchResult.searchValue) { ImBool(true) }

            if (!openState.get()) {
                panelsOpenState.remove(searchResult.searchValue)
                searchResIterator.remove()
                continue
            }

            window("Search Result: ${searchResult.searchValue} (${searchResult.positions.size})###${searchResult.searchValue}", openState) {
                if (inputText("##replace_type", replaceType, "Replace Type")) {
                    if (replaceType.length > 0) {
                        checkReplaceEnabled()
                    }
                }
                sameLine()
                if (replaceType.length == 0) {
                    button("Delete All##delete_all_${searchResult.searchValue}") {
                        deleteAll(searchResult)
                        searchResIterator.remove()
                    }
                } else {
                    button("Replace All##replace_all_${searchResult.searchValue}") {
                        replaceAll(searchResult)
                        searchResIterator.remove()
                    }
                }
                sameLine()
                helpMark("Provide type to Replace, keep empty to Delete\nLMB - jump to instance\nRMB - replace/delete instance")

                if (!isReplaceEnabled && replaceType.length > 0) {
                    textColored(1f, 0f, 0f, 1f, "Replace type doesn't exist")
                }

                separator()

                child("search_result_positions") {
                    columns(getWindowWidth().toInt() / 100, "search_result_columns", false)

                    val posIterator = searchResult.positions.listIterator()
                    var idx = 0

                    while (posIterator.hasNext()) {
                        val searchPos = posIterator.next()

                        button("x:%03d y:%03d##jump_btn_${idx++}".format(searchPos.pos.x, searchPos.pos.y)) {
                            sendEvent(Event.CanvasController.CenterPosition(searchPos.pos))
                            sendEvent(Event.CanvasController.MarkPosition(searchPos.pos))
                        }
                        setItemHoveredTooltip("[${searchPos.idx}]")

                        if (isItemClicked(RMB)) {
                            if (replaceType.length == 0) {
                                delete(searchPos, searchResult.isSearchById)
                                posIterator.remove()
                            } else if (isReplaceEnabled) {
                                replace(searchPos, searchResult.isSearchById)
                                posIterator.remove()
                            }
                        }

                        nextColumn()
                    }
                }
            }
        }
    }

    private fun checkReplaceEnabled() {
        sendEvent(Event.EnvironmentController.Fetch {
            isReplaceEnabled = it.getItem(replaceType.get()) != null
        })
    }

    private fun delete(searchPosition: SearchPosition, isSearchById: Boolean) {
        delete(mutableListOf(Pair(searchPosition.tileItem, searchPosition.pos)), isSearchById)
    }

    private fun deleteAll(searchResult: SearchResult) {
        delete(searchResult.positions.asSequence().map { Pair(it.tileItem, it.pos) }.toList(), searchResult.isSearchById)
    }

    private fun delete(deletionList: List<Pair<TileItem, MapPos>>, isSearchById: Boolean) {
        if (isSearchById) {
            sendEvent(Event.MapModifierController.DeleteIdInPositions(deletionList))
        } else {
            sendEvent(Event.MapModifierController.DeleteTypeInPositions(deletionList))
        }

        sendEvent(Event.CanvasController.ResetMarkedPosition())
    }

    private fun replace(searchPosition: SearchPosition, isSearchById: Boolean) {
        replace(mutableListOf(Pair(searchPosition.tileItem, searchPosition.pos)), isSearchById)
    }

    private fun replaceAll(searchResult: SearchResult) {
        replace(searchResult.positions.asSequence().map { Pair(it.tileItem, it.pos) }.toList(), searchResult.isSearchById)
    }

    private fun replace(replaceList: List<Pair<TileItem, MapPos>>, isSearchById: Boolean) {
        if (!isReplaceEnabled) {
            return
        }

        if (isSearchById) {
            sendEvent(Event.MapModifierController.ReplaceIdInPositions(Pair(replaceType.get(), replaceList)))
        } else {
            sendEvent(Event.MapModifierController.ReplaceTypeInPositions(Pair(replaceType.get(), replaceList)))
        }

        sendEvent(Event.CanvasController.ResetMarkedPosition())
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
            currentMapId = Dmm.MAP_ID_NONE
            clearAll()
        }
    }

    private fun handleOpen(event: Event<SearchResult, Unit>) {
        searchResults.remove(event.body)
        searchResults.add(event.body)
    }
}
