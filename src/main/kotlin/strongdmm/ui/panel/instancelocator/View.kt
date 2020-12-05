package strongdmm.ui.panel.instancelocator

import imgui.ImGui
import strongdmm.util.imgui.ImGuiExt
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.imGuiButton
import strongdmm.util.imgui.imGuiBegin
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
            get() = ImGui.getWindowWidth() - (75f * Window.pointSize)
        private val searchCoordInputWidth: Float
            get() = 100f * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        if (!state.doInstanceLocatorOpen.get()) {
            state.isFirstOpen = true
            return
        }

        ImGuiUtil.setNextWindowCentered(width, height, Window.windowCond)

        imGuiBegin(TITLE, state.doInstanceLocatorOpen) {
            if (state.isFirstOpen) {
                ImGui.setKeyboardFocusHere()
                state.isFirstOpen = false
            }

            ImGui.setNextItemWidth(searchTypeInputWidth)
            ImGuiExt.inputTextPlaceholder("##search_type", state.searchType, "Search Type")
            ImGui.sameLine()

            imGuiButton("Search", block = viewController::doSearch)

            ImGui.newLine()
            ImGui.text("Search Rect:")
            ImGui.setNextItemWidth(searchCoordInputWidth)
            ImGuiExt.inputIntClamp("x1", state.searchX1, 1, state.mapMaxX)
            ImGui.sameLine()
            ImGui.setNextItemWidth(searchCoordInputWidth)
            ImGuiExt.inputIntClamp("y1", state.searchY1, 1, state.mapMaxY)
            ImGui.setNextItemWidth(searchCoordInputWidth)
            ImGuiExt.inputIntClamp("x2", state.searchX2, 1, state.mapMaxX)
            ImGui.sameLine()
            ImGui.setNextItemWidth(searchCoordInputWidth)
            ImGuiExt.inputIntClamp("y2", state.searchY2, 1, state.mapMaxY)

            imGuiButton("Selection", block = viewController::doSelection)
            ImGui.sameLine()
            imGuiButton("Reset", block = viewController::doReset)
        }
    }
}
