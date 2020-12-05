package strongdmm.ui.panel.instance_locator

import imgui.ImGui.*
import strongdmm.util.imgui.ImGuiExt
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.button
import strongdmm.util.imgui.window
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 300f * Window.pointSize
        private val height: Float
            get() = 180f * Window.pointSize

        private const val TITLE: String = "Instance Locator"

        private val searchTypeInputWidth: Float
            get() = getWindowWidth() - (75f * Window.pointSize)
        private val searchCoordInputWidth: Float
            get() = 100f * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        if (!state.showInstanceLocator.get()) {
            state.isFirstOpen = true
            return
        }

        ImGuiUtil.setNextWindowCentered(width, height, Window.windowCond)

        window(TITLE, state.showInstanceLocator) {
            if (state.isFirstOpen) {
                setKeyboardFocusHere()
                state.isFirstOpen = false
            }

            setNextItemWidth(searchTypeInputWidth)
            ImGuiExt.inputTextPlaceholder("##search_type", state.searchType, "Search Type")
            sameLine()

            button("Search", block = viewController::doSearch)

            newLine()
            text("Search Rect:")
            setNextItemWidth(searchCoordInputWidth)
            ImGuiExt.inputIntClamp("x1", state.searchX1, 1, state.mapMaxX)
            sameLine()
            setNextItemWidth(searchCoordInputWidth)
            ImGuiExt.inputIntClamp("y1", state.searchY1, 1, state.mapMaxY)
            setNextItemWidth(searchCoordInputWidth)
            ImGuiExt.inputIntClamp("x2", state.searchX2, 1, state.mapMaxX)
            sameLine()
            setNextItemWidth(searchCoordInputWidth)
            ImGuiExt.inputIntClamp("y2", state.searchY2, 1, state.mapMaxY)

            button("Selection", block = viewController::doSelection)
            sameLine()
            button("Reset", block = viewController::doReset)
        }
    }
}
