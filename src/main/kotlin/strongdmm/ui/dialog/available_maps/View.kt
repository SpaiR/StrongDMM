package strongdmm.ui.dialog.available_maps

import imgui.ImGui
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW
import strongdmm.util.imgui.*
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 600f * Window.pointSize
        private val height: Float
            get() = 285f * Window.pointSize

        private const val TITLE: String = "Available Maps"

        private val mapListWidthIndent: Float
            get() = 20 * Window.pointSize
        private val mapListHeightIndent: Float
            get() = 110 * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.isDoOpen) {
            ImGui.openPopup(TITLE)
            state.isDoOpen = false
        }

        ImGuiUtil.setNextWindowCentered(width, height, ImGuiCond.Appearing)

        imGuiPopupModal(TITLE) {
            ImGui.text("Selected: ${state.selectedMapPath?.readable ?: ""}")

            ImGui.setNextItemWidth(ImGui.getWindowWidth() - 20)

            if (state.isFirstOpen) {
                ImGui.setKeyboardFocusHere()
                state.isFirstOpen = false
            }

            ImGuiExt.inputTextPlaceholder("##maps_path_filter", state.mapFilter, "Paths Filter")

            val width = ImGui.getWindowWidth() - mapListWidthIndent
            val height = ImGui.getWindowHeight() - mapListHeightIndent

            imGuiChild("available_maps_list", width, height, true, ImGuiWindowFlags.HorizontalScrollbar) {
                for (mapPath in state.providedAvailableMapPaths) {
                    if (viewController.isFilteredOutVisibleFilePath(mapPath.readable)) {
                        continue
                    }

                    ImGui.bullet()
                    ImGui.sameLine()
                    imGuiSelectable(mapPath.readable, state.selectedMapPath === mapPath) {
                        viewController.doSelectMapPath(mapPath)
                    }
                }
            }

            val isMapNotSelected = state.selectedMapPath == null

            if (isMapNotSelected) {
                ImGuiUtil.pushDisabledItem()
            }

            imGuiButton("Open", block = viewController::doOpenSelectedMapAndDispose)

            if (isMapNotSelected) {
                ImGuiUtil.popDisabledItem()
            }

            ImGui.sameLine()
            imGuiButton("Cancel", block = viewController::dispose)

            if (ImGui.isKeyPressed(GLFW.GLFW_KEY_ENTER) || ImGui.isKeyPressed(GLFW.GLFW_KEY_KP_ENTER)) {
                viewController.doOpenSelectedMapAndDispose()
            } else if (ImGui.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                viewController.dispose()
            }
        }
    }
}
