package strongdmm.ui.panel.search_result

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiMouseButton
import strongdmm.ui.panel.search_result.model.SearchResult
import strongdmm.util.imgui.*
import strongdmm.application.window.Window

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

            imGuiBegin("Search Result: ${searchResult.searchObject} (${searchResult.positions.size})###search_result", state.isOpen) {
                showControls(searchResult)
                ImGui.separator()
                showSearchPositions(searchResult)
            }

            viewController.checkSearchPositionsToRemove()
        }
    }

    private fun showControls(searchResult: SearchResult) {
        if (ImGuiExt.inputTextPlaceholder("##replace_type", state.replaceType, "Replace Type")) {
            if (state.replaceType.length > 0) {
                viewController.checkReplaceModeEnabled()
            }
        }

        ImGui.sameLine()

        if (state.replaceType.length == 0) {
            imGuiButton("Delete All##delete_all_${searchResult.searchObject}", block = viewController::doDeleteAll)
        } else {
            if (!state.isReplaceEnabled) {
                ImGui.pushStyleColor(ImGuiCol.Button, COLOR_GREY)
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, COLOR_GREY)
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, COLOR_GREY)
            }

            imGuiButton("Replace All##replace_all_${searchResult.searchObject}", block = viewController::doReplaceAll)

            if (!state.isReplaceEnabled) {
                ImGui.popStyleColor(3)
            }
        }

        ImGui.sameLine()

        ImGuiExt.helpMark("Provide type to Replace, keep empty to Delete\nLMB - jump to instance\nRMB - replace/delete instance")

        if (!state.isReplaceEnabled && state.replaceType.length > 0) {
            ImGui.textColored(COLOR_RED, "Replace type doesn't exist")
        }
    }

    private fun showSearchPositions(searchResult: SearchResult) {
        imGuiChild("search_result_positions") {
            ImGui.columns(Math.max(1, ImGui.getWindowWidth().toInt() / columnWidth.toInt()), "search_result_columns", false)

            searchResult.positions.forEachIndexed { index, searchPosition ->
                imGuiButton("x:%03d y:%03d z:%02d##jump_btn_$index".format(searchPosition.pos.x, searchPosition.pos.y, searchPosition.pos.z)) {
                    viewController.doJump(searchPosition)
                }

                if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
                    if (state.replaceType.length == 0) {
                        viewController.doDelete(searchPosition)
                    } else if (state.isReplaceEnabled) {
                        viewController.doReplace(searchPosition)
                    }
                }

                ImGui.nextColumn()
            }
        }
    }
}
