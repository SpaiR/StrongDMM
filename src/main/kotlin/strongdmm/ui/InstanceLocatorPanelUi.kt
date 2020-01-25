package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiCond
import org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
import org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItemId
import strongdmm.byond.dmm.TileItemType
import strongdmm.event.*
import strongdmm.ui.search.SearchResult
import strongdmm.util.imgui.inputText
import strongdmm.util.imgui.window

class InstanceLocatorPanelUi : EventSender, EventConsumer {
    private val showInstanceLocator: ImBool = ImBool(false)
    private var isFirstOpen: Boolean = true

    private val searchType: ImString = ImString(50).apply { inputData.isResizable = true }

    init {
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.InstanceLocatorPanelUi.SearchByType::class.java, ::handleSearchByType)
        consumeEvent(Event.InstanceLocatorPanelUi.SearchById::class.java, ::handleSearchById)
    }

    fun postInit() {
        sendEvent(Event.Global.Provider.InstanceLocatorOpen(showInstanceLocator))
    }

    fun process() {
        if (!showInstanceLocator.get()) {
            isFirstOpen = true
            return
        }

        setNextWindowPos(345f, 535f, ImGuiCond.Once)
        setNextWindowSize(300f, 97.5f, ImGuiCond.Once)

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
        }
    }

    private fun search() {
        val type = searchType.get().trim()

        if (type.isEmpty()) {
            return
        }

        val tileItemId = type.toLongOrNull()

        val openSearchResult = { it: List<Pair<TileItemType, MapPos>> ->
            if (it.isNotEmpty()) {
                sendEvent(Event.SearchResultPanelUi.Open(SearchResult(type, it)))
            }
        }

        if (tileItemId != null) {
            sendEvent(Event.InstanceController.FindPositionsById(tileItemId, openSearchResult))
        } else {
            sendEvent(Event.InstanceController.FindPositionsByType(type, openSearchResult))
        }
    }

    private fun handleResetEnvironment() {
        searchType.set("")
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
