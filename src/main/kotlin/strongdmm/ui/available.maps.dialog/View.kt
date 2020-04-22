package strongdmm.ui.available.maps.dialog

import imgui.ImGui.*
import imgui.enums.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW
import strongdmm.util.imgui.*

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 600f
        private const val HEIGHT: Float = 285f

        private const val TITLE: String = "Available Maps"
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.isDoOpen) {
            openPopup(TITLE)
            state.isDoOpen = false
        }

        WindowUtil.setNextSize(WIDTH, HEIGHT)

        popupModal(TITLE) {
            text("Selected: ${state.selectionStatus}")

            setNextItemWidth(getWindowWidth() - 20)

            if (state.isFirstOpen) {
                setKeyboardFocusHere()
                state.isFirstOpen = false
            }

            inputText("##maps_path_filter", state.mapFilter, "Paths Filter")

            child("available_maps_list", getWindowWidth() - 20, getWindowHeight() - 100, true, ImGuiWindowFlags.HorizontalScrollbar) {
                for ((absoluteFilePath, visibleFilePath) in state.providedAvailableMaps) {
                    if (viewController.isFilteredVisibleFilePath(visibleFilePath)) {
                        continue
                    }

                    bullet()
                    sameLine()
                    selectable(visibleFilePath, state.selectedAbsMapPath == absoluteFilePath) {
                        viewController.doSelectMapPath(absoluteFilePath, visibleFilePath)
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
