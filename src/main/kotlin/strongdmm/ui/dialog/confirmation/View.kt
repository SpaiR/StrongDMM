package strongdmm.ui.dialog.confirmation

import imgui.ImGui
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiWindowFlags
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogType
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.imGuiButton
import strongdmm.util.imgui.imGuiPopupModal
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 400f * Window.pointSize
        private val height: Float
            get() = 100f * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        val title = state.data.title + "##confirmation_dialog"

        if (state.isDoOpen) {
            ImGui.openPopup(title)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        ImGuiUtil.setNextWindowCentered(width, height, ImGuiCond.Appearing)

        imGuiPopupModal(title, ImGuiWindowFlags.NoResize) {
            ImGui.textWrapped(state.data.question)
            ImGui.newLine()
            imGuiButton("Yes", block = viewController::doYes)
            ImGui.sameLine()
            imGuiButton("No", block = viewController::doNo)

            if (state.data.type == ConfirmationDialogType.YES_NO_CANCEL) {
                ImGui.sameLine()
                imGuiButton("Cancel", block = viewController::doCancel)
            }
        }
    }
}
