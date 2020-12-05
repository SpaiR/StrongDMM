package strongdmm.ui.dialog.set_map_size

import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImInt
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.imGuiButton
import strongdmm.util.imgui.imGuiPopupModal
import strongdmm.application.window.Window

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
            ImGui.openPopup(TITLE)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        ImGuiUtil.setNextWindowCentered(width, height)

        imGuiPopupModal(TITLE, ImGuiWindowFlags.NoResize) {
            showInput("X", state.newX)
            ImGui.sameLine()
            showInput("Y", state.newY)
            ImGui.sameLine()
            showInput("Z", state.newZ)

            imGuiButton("OK", block = viewController::doOk)
            ImGui.sameLine()
            imGuiButton("Cancel", block = viewController::doCancel)
        }
    }

    private fun showInput(label: String, data: ImInt) {
        ImGui.setNextItemWidth(sizeInputWidth)
        if (ImGui.inputInt(label, data)) {
            if (data.get() <= 0) {
                data.set(1)
            }
        }
    }
}
