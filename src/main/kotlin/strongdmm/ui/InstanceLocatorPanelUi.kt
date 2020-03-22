package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImInt
import imgui.ImString
import imgui.enums.ImGuiCond
import org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
import org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.*
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventInstanceController
import strongdmm.event.type.controller.EventToolsController
import strongdmm.event.type.ui.EventInstanceLocatorPanelUi
import strongdmm.event.type.ui.EventSearchResultPanelUi
import strongdmm.ui.search.SearchRect
import strongdmm.ui.search.SearchResult
import strongdmm.util.imgui.button
import strongdmm.util.imgui.inputInt
import strongdmm.util.imgui.inputText
import strongdmm.util.imgui.window

class InstanceLocatorPanelUi : EventSender, EventConsumer {
    companion object {
        private const val SEARCH_INPUT_WIDTH: Float = 100f
    }

    private val showInstanceLocator: ImBool = ImBool(false)
    private var isFirstOpen: Boolean = true

    private val searchType: ImString = ImString(50).apply { inputData.isResizable = true }

    private var mapMaxX: Int = 255
    private var mapMaxY: Int = 255

    private val searchX1: ImInt = ImInt(1)
    private val searchY1: ImInt = ImInt(1)
    private val searchX2: ImInt = ImInt(255)
    private val searchY2: ImInt = ImInt(255)

    init {
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(EventInstanceLocatorPanelUi.SearchByType::class.java, ::handleSearchByType)
        consumeEvent(EventInstanceLocatorPanelUi.SearchById::class.java, ::handleSearchById)
    }

    fun postInit() {
        sendEvent(EventGlobalProvider.InstanceLocatorOpen(showInstanceLocator))
    }

    fun process() {
        if (!showInstanceLocator.get()) {
            isFirstOpen = true
            return
        }

        setNextWindowPos(345f, 535f, ImGuiCond.Once)
        setNextWindowSize(300f, 180f, ImGuiCond.Once)

        window("Instance Locator", showInstanceLocator) {
            if (isFirstOpen) {
                setKeyboardFocusHere()
                isFirstOpen = false
            }

            setNextItemWidth(getWindowWidth() - 75)
            inputText("##search_type", searchType, "Search Type")
            sameLine()
            if (button("Search") || isKeyPressed(GLFW_KEY_ENTER) || isKeyPressed(GLFW_KEY_KP_ENTER)) {
                search()
            }

            newLine()
            text("Search Rect:")
            setNextItemWidth(SEARCH_INPUT_WIDTH)
            inputInt("x1", searchX1, 1, mapMaxX)
            sameLine()
            setNextItemWidth(SEARCH_INPUT_WIDTH)
            inputInt("y1", searchY1, 1, mapMaxY)
            setNextItemWidth(SEARCH_INPUT_WIDTH)
            inputInt("x2", searchX2, 1, mapMaxX)
            sameLine()
            setNextItemWidth(SEARCH_INPUT_WIDTH)
            inputInt("y2", searchY2, 1, mapMaxY)

            button("Selection", block = ::setSearchRectToActiveArea)
            sameLine()
            button("Reset", block = ::resetSearchRect)
        }
    }

    private fun setSearchRectToActiveArea() {
        sendEvent(EventToolsController.FetchActiveArea { activeArea ->
            searchX1.set(activeArea.x1)
            searchY1.set(activeArea.y1)
            searchX2.set(activeArea.x2)
            searchY2.set(activeArea.y2)
        })
    }

    private fun resetSearchRect() {
        searchX1.set(1)
        searchY1.set(1)
        searchX2.set(mapMaxX)
        searchY2.set(mapMaxY)
    }

    private fun search() {
        val type = searchType.get().trim()

        if (type.isEmpty()) {
            return
        }

        val tileItemId = type.toLongOrNull()
        val searchRect = SearchRect(searchX1.get(), searchY1.get(), searchX2.get(), searchY2.get())

        val openSearchResult = { it: List<Pair<TileItem, MapPos>> ->
            if (it.isNotEmpty()) {
                sendEvent(EventSearchResultPanelUi.Open(SearchResult(type, tileItemId != null, it)))
            }
        }

        if (tileItemId != null) {
            sendEvent(EventInstanceController.FindInstancePositionsById(Pair(searchRect, tileItemId), openSearchResult))
        } else {
            sendEvent(EventInstanceController.FindInstancePositionsByType(Pair(searchRect, type), openSearchResult))
        }
    }

    private fun handleEnvironmentReset() {
        searchType.set("")
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        mapMaxX = event.body.maxX
        mapMaxY = event.body.maxY

        searchX2.set(mapMaxX)
        searchY2.set(mapMaxY)
    }

    private fun handleSearchByType(event: Event<TileItemType, Unit>) {
        showInstanceLocator.set(true)
        searchType.set(event.body)
        search()
    }

    private fun handleSearchById(event: Event<TileItemId, Unit>) {
        showInstanceLocator.set(true)
        searchType.set(event.body.toString())
        search()
    }
}
