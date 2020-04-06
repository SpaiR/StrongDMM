package strongdmm.ui.search

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiMouseButton
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.EventCanvasController
import strongdmm.event.type.controller.EventEnvironmentController
import strongdmm.event.type.controller.EventMapModifierController
import strongdmm.event.type.ui.EventSearchResultPanelUi
import strongdmm.util.imgui.*
import strongdmm.window.AppWindow

class SearchResultPanelUi : EventConsumer, EventSender {
    private val searchResults: MutableMap<String, SearchResult> = mutableMapOf()
    private val panelsOpenState: MutableMap<String, ImBool> = mutableMapOf()

    private var selectedMapId: Int = Dmm.MAP_ID_NONE

    private val replaceType: ImString = ImString(50).apply { inputData.isResizable = true }
    private var isReplaceEnabled: Boolean = false

    init {
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(EventGlobal.SelectedMapZActiveChanged::class.java, ::handleSelectedMapZActiveChanged)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(EventSearchResultPanelUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (searchResults.isEmpty()) {
            return
        }

        val searchResIterator = searchResults.iterator()
        while (searchResIterator.hasNext()) {
            val (_, searchResult) = searchResIterator.next()

            if (searchResult.positions.isEmpty()) {
                searchResIterator.remove()
                continue
            }

            val openState = panelsOpenState.getOrPut(searchResult.searchValue) { ImBool(true) }

            if (!openState.get()) {
                panelsOpenState.remove(searchResult.searchValue)
                searchResIterator.remove()
                continue
            }

            setNextWindowPos((AppWindow.windowWidth - 357f) / 2, (AppWindow.windowHeight - 390f) / 2, AppWindow.defaultWindowCond)
            setNextWindowSize(375f, 390f, AppWindow.defaultWindowCond)

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
                    if (!isReplaceEnabled) {
                        pushStyleColor(ImGuiCol.Button, GREY32)
                        pushStyleColor(ImGuiCol.ButtonHovered, GREY32)
                        pushStyleColor(ImGuiCol.ButtonActive, GREY32)
                    }

                    button("Replace All##replace_all_${searchResult.searchValue}") {
                        if (isReplaceEnabled) {
                            replaceAll(searchResult)
                            searchResIterator.remove()
                        }
                    }

                    if (!isReplaceEnabled) {
                        popStyleColor(3)
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
                            sendEvent(EventCanvasController.CenterCanvasByPosition(searchPos.pos))
                            sendEvent(EventCanvasController.MarkPosition(searchPos.pos))
                        }

                        if (isItemClicked(ImGuiMouseButton.Right)) {
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

        if (panelsOpenState.isEmpty()) {
            sendEvent(EventCanvasController.ResetMarkedPosition())
        }
    }

    private fun checkReplaceEnabled() {
        sendEvent(EventEnvironmentController.FetchOpenedEnvironment {
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
            sendEvent(EventMapModifierController.DeleteTileItemsWithIdInPositions(deletionList))
        } else {
            sendEvent(EventMapModifierController.DeleteTileItemsWithTypeInPositions(deletionList))
        }

        sendEvent(EventCanvasController.ResetMarkedPosition())
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
            sendEvent(EventMapModifierController.ReplaceTileItemsWithIdInPositions(Pair(replaceType.get(), replaceList)))
        } else {
            sendEvent(EventMapModifierController.ReplaceTileItemsWithTypeInPositions(Pair(replaceType.get(), replaceList)))
        }

        sendEvent(EventCanvasController.ResetMarkedPosition())
    }

    private fun clearAll() {
        searchResults.clear()
        panelsOpenState.clear()
    }

    private fun handleEnvironmentReset() {
        clearAll()
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        selectedMapId = event.body.id
        clearAll()
    }

    private fun handleSelectedMapZActiveChanged() {
        clearAll()
    }

    private fun handleOpenedMapClosed(event: Event<Dmm, Unit>) {
        if (event.body.id == selectedMapId) {
            selectedMapId = Dmm.MAP_ID_NONE
            clearAll()
        }
    }

    private fun handleOpen(event: Event<SearchResult, Unit>) {
        searchResults[event.body.searchValue] = event.body
    }
}
