package strongdmm.ui.panel.search_result

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiMouseButton
import strongdmm.ui.panel.search_result.model.SearchResult
import strongdmm.util.imgui.*
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 375f * Window.pointSize
        private val height: Float
            get() = 390f * Window.pointSize

        private val columnWidth: Float
            get() = 125f * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        state.searchResult?.let { searchResult ->
            if (!state.isOpen.get()) {
                viewController.dispose()
                return
            }

            ImGuiUtil.setNextWindowCentered(width, height)

            window("Search Result: ${searchResult.searchObject} (${searchResult.positions.size})###search_result", state.isOpen) {
                showControls(searchResult)
                separator()
                showSearchPositions(searchResult)
            }

            viewController.checkSearchPositionsToRemove()
        }
    }

    private fun showControls(searchResult: SearchResult) {
        if (inputText("##replace_type", state.replaceType, "Replace Type")) {
            if (state.replaceType.length > 0) {
                viewController.checkReplaceModeEnabled()
            }
        }

        sameLine()

        if (state.replaceType.length == 0) {
            button("Delete All##delete_all_${searchResult.searchObject}", block = viewController::doDeleteAll)
        } else {
            if (!state.isReplaceEnabled) {
                pushStyleColor(ImGuiCol.Button, GREY32)
                pushStyleColor(ImGuiCol.ButtonHovered, GREY32)
                pushStyleColor(ImGuiCol.ButtonActive, GREY32)
            }

            button("Replace All##replace_all_${searchResult.searchObject}", block = viewController::doReplaceAll)

            if (!state.isReplaceEnabled) {
                popStyleColor(3)
            }
        }

        sameLine()

        helpMark("Provide type to Replace, keep empty to Delete\nLMB - jump to instance\nRMB - replace/delete instance")

        if (!state.isReplaceEnabled && state.replaceType.length > 0) {
            textColored(1f, 0f, 0f, 1f, "Replace type doesn't exist")
        }
    }

    private fun showSearchPositions(searchResult: SearchResult) {
        child("search_result_positions") {
            columns(Math.max(1, getWindowWidth().toInt() / columnWidth.toInt()), "search_result_columns", false)

            searchResult.positions.forEachIndexed { index, searchPosition ->
                button("x:%03d y:%03d z:%02d##jump_btn_$index".format(searchPosition.pos.x, searchPosition.pos.y, searchPosition.pos.z)) {
                    viewController.doJump(searchPosition)
                }

                if (isItemClicked(ImGuiMouseButton.Right)) {
                    if (state.replaceType.length == 0) {
                        viewController.doDelete(searchPosition)
                    } else if (state.isReplaceEnabled) {
                        viewController.doReplace(searchPosition)
                    }
                }

                nextColumn()
            }
        }
    }
}
