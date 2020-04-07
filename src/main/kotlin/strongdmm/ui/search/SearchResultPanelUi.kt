package strongdmm.ui.search

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiMouseButton
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerCanvasController
import strongdmm.event.type.controller.TriggerEnvironmentController
import strongdmm.event.type.controller.TriggerMapModifierController
import strongdmm.event.type.ui.TriggerSearchResultPanelUi
import strongdmm.util.imgui.*
import strongdmm.window.AppWindow

class SearchResultPanelUi : EventConsumer, EventSender {
    private val searchResults: MutableMap<String, SearchResult> = mutableMapOf()
    private val panelsOpenState: MutableMap<String, ImBool> = mutableMapOf()

    private val replaceType: ImString = ImString(50).apply { inputData.isResizable = true }
    private var isReplaceEnabled: Boolean = false

    init {
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapZActiveChanged::class.java, ::handleSelectedMapZActiveChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(TriggerSearchResultPanelUi.Open::class.java, ::handleOpen)
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
                            sendEvent(TriggerCanvasController.CenterCanvasByPosition(searchPos.pos))
                            sendEvent(TriggerCanvasController.MarkPosition(searchPos.pos))
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
            sendEvent(TriggerCanvasController.ResetMarkedPosition())
        }
    }

    private fun checkReplaceEnabled() {
        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment {
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
            sendEvent(TriggerMapModifierController.DeleteTileItemsWithIdInPositions(deletionList))
        } else {
            sendEvent(TriggerMapModifierController.DeleteTileItemsWithTypeInPositions(deletionList))
        }

        sendEvent(TriggerCanvasController.ResetMarkedPosition())
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
            sendEvent(TriggerMapModifierController.ReplaceTileItemsWithIdInPositions(Pair(replaceType.get(), replaceList)))
        } else {
            sendEvent(TriggerMapModifierController.ReplaceTileItemsWithTypeInPositions(Pair(replaceType.get(), replaceList)))
        }

        sendEvent(TriggerCanvasController.ResetMarkedPosition())
    }

    private fun clearAll() {
        searchResults.clear()
        panelsOpenState.clear()
    }

    private fun handleEnvironmentReset() {
        clearAll()
    }

    private fun handleSelectedMapChanged() {
        clearAll()
    }

    private fun handleSelectedMapZActiveChanged() {
        clearAll()
    }

    private fun handleSelectedMapClosed() {
        clearAll()
    }

    private fun handleOpen(event: Event<SearchResult, Unit>) {
        searchResults[event.body.searchValue] = event.body
    }
}
