package strongdmm.ui.dialog.confirmation

import imgui.ImGui.*
import imgui.enums.ImGuiWindowFlags
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogType
import strongdmm.util.imgui.WindowUtil
import strongdmm.util.imgui.button
import strongdmm.util.imgui.popupModal

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 400f
        private const val HEIGHT: Float = 100f
    }

    lateinit var viewController: ViewController

    fun process() {
        val title = state.data.title + "##confirmation_dialog_${state.windowId}"

        if (state.isDoOpen) {
            openPopup(title)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        WindowUtil.setNextSize(WIDTH, HEIGHT)

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
