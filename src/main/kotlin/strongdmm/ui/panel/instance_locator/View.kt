package strongdmm.ui.panel.instance_locator

import imgui.ImGui.*
import strongdmm.util.imgui.*
import strongdmm.util.imgui.button
import strongdmm.util.imgui.inputInt

class View(
    private val state: State
) {
    companion object {
        private const val POS_X: Float = 350f
        private const val POS_Y_PERCENT: Int = 60

        private const val WIDTH: Float = 300f
        private const val HEIGHT: Float = 180f

        private const val TITLE: String = "Instance Locator"

        private const val SEARCH_INPUT_WIDTH: Float = 100f
    }

    lateinit var viewController: ViewController

    fun process() {
        if (!state.showInstanceLocator.get()) {
            state.isFirstOpen = true
            return
        }

        WindowUtil.setNextPosAndSize(POS_X, WindowUtil.getHeightPercent(POS_Y_PERCENT), WIDTH, HEIGHT)

        window(TITLE, state.showInstanceLocator) {
            if (state.isFirstOpen) {
                setKeyboardFocusHere()
                state.isFirstOpen = false
            }

            setNextItemWidth(getWindowWidth() - 75)
            inputText("##search_type", state.searchType, "Search Type")
            sameLine()

            button("Search", block = viewController::doSearch)

            newLine()
            text("Search Rect:")
            setNextItemWidth(SEARCH_INPUT_WIDTH)
            inputInt("x1", state.searchX1, 1, state.mapMaxX)
            sameLine()
            setNextItemWidth(SEARCH_INPUT_WIDTH)
            inputInt("y1", state.searchY1, 1, state.mapMaxY)
            setNextItemWidth(SEARCH_INPUT_WIDTH)
            inputInt("x2", state.searchX2, 1, state.mapMaxX)
            sameLine()
            setNextItemWidth(SEARCH_INPUT_WIDTH)
            inputInt("y2", state.searchY2, 1, state.mapMaxY)

            button("Selection", block = viewController::doSelection)
            sameLine()
            button("Reset", block = viewController::doReset)
        }
    }
}
