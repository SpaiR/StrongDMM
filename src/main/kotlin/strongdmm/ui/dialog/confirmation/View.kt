package strongdmm.ui.dialog.confirmation

import imgui.ImGui.*
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiWindowFlags
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogType
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.button
import strongdmm.util.imgui.popupModal
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
            openPopup(title)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        ImGuiUtil.setNextWindowCentered(width, height, ImGuiCond.Appearing)

        popupModal(title, ImGuiWindowFlags.NoResize) {
            textWrapped(state.data.question)
            newLine()
            button("Yes", block = viewController::doYes)
            sameLine()
            button("No", block = viewController::doNo)

            if (state.data.type == ConfirmationDialogType.YES_NO_CANCEL) {
                sameLine()
                button("Cancel", block = viewController::doCancel)
            }
        }
    }
}
