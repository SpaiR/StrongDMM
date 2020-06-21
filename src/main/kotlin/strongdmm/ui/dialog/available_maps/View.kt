package strongdmm.ui.dialog.available_maps

import imgui.ImGui.*
import imgui.enums.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW
import strongdmm.util.imgui.*
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 600f * Window.pointSize
        private val height: Float
            get() = 285f * Window.pointSize

        private const val TITLE: String = "Available Maps"
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.isDoOpen) {
            openPopup(TITLE)
            state.isDoOpen = false
        }

        ImGuiUtil.setNextWindowCentered(width, height)

        popupModal(TITLE) {
            text("Selected: ${state.selectedMapPath?.readable ?: ""}")

            setNextItemWidth(getWindowWidth() - 20)

            if (state.isFirstOpen) {
                setKeyboardFocusHere()
                state.isFirstOpen = false
            }

            inputText("##maps_path_filter", state.mapFilter, "Paths Filter")

            child("available_maps_list", getWindowWidth() - 20, getWindowHeight() - 110, true, ImGuiWindowFlags.HorizontalScrollbar) {
                for (mapPath in state.providedAvailableMapPaths) {
                    if (viewController.isFilteredOutVisibleFilePath(mapPath.readable)) {
                        continue
                    }

                    bullet()
                    sameLine()
                    selectable(mapPath.readable, state.selectedMapPath === mapPath) {
                        viewController.doSelectMapPath(mapPath)
                    }
                }
            }

            button("Open", block = viewController::doOpenSelectedMapAndDispose)
            sameLine()
            button("Cancel", block = viewController::dispose)

            if (isKeyPressed(GLFW.GLFW_KEY_ENTER) || isKeyPressed(GLFW.GLFW_KEY_KP_ENTER)) {
                viewController.doOpenSelectedMapAndDispose()
            } else if (isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                viewController.dispose()
            }
        }
    }
}
