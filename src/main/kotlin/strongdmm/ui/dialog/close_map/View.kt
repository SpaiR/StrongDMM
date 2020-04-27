package strongdmm.ui.dialog.close_map_dialog

import imgui.ImGui.*
import imgui.enums.ImGuiWindowFlags
import strongdmm.util.imgui.WindowUtil
import strongdmm.util.imgui.button
import strongdmm.util.imgui.popupModal

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 400f
        private const val HEIGHT: Float = 100f

        private const val TITLE: String = "Save Map?"
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.isDoOpen) {
            openPopup(TITLE)
            viewController.blockApplication()
            state.isDoOpen = false
        }

        WindowUtil.setNextSize(WIDTH, HEIGHT)

        popupModal(TITLE, ImGuiWindowFlags.NoResize) {
            text("Map \"${state.eventToReply?.body?.mapName}\" has been modified. Save changes?")
            newLine()
            button("Yes", block = viewController::doDisposeWithSave)
            sameLine()
            button("No", block = viewController::doDisposeWithoutSave)
            sameLine()
            button("Cancel", block = viewController::doDisposeWithCancel)
        }
    }
}
