package strongdmm.ui.dialog.set_map_size

import imgui.ImGui.*
import imgui.ImInt
import imgui.enums.ImGuiWindowFlags
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.button
import strongdmm.util.imgui.popupModal
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 370f * Window.pointSize
        private val height: Float
            get() = 100f * Window.pointSize

        private const val TITLE: String = "Set Map Size"

        private val sizeInputWidth: Float
            get() = 100f * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.isDoOpen) {
            openPopup(TITLE)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        ImGuiUtil.setNextWindowCentered(width, height)

        popupModal(TITLE, ImGuiWindowFlags.NoResize) {
            showInput("X", state.newX)
            sameLine()
            showInput("Y", state.newY)
            sameLine()
            showInput("Z", state.newZ)

            button("OK", block = viewController::doOk)
            sameLine()
            button("Cancel", block = viewController::doCancel)
        }
    }

    private fun showInput(label: String, data: ImInt) {
        setNextItemWidth(sizeInputWidth)
        if (inputInt(label, data)) {
            if (data.get() <= 0) {
                data.set(1)
            }
        }
    }
}
