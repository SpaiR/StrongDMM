package strongdmm.ui.dialog.set_map_size

import imgui.ImGui.*
import imgui.ImInt
import imgui.enums.ImGuiWindowFlags
import strongdmm.util.imgui.WindowUtil
import strongdmm.util.imgui.button
import strongdmm.util.imgui.popupModal

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 295f
        private const val HEIGHT: Float = 100f

        private const val TITLE: String = "Set Map Size"
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
            showInput("X", state.newX)
            sameLine()
            showInput("Y", state.newY)
            sameLine()
            showInput("Z", state.newZ)

            newLine()
            button("OK", block = viewController::doOk)
            sameLine()
            button("Cancel", block = viewController::doCancel)
        }
    }

    private fun showInput(label: String, data: ImInt) {
        setNextItemWidth(75f)
        if (inputInt(label, data)) {
            if (data.get() <= 0) {
                data.set(1)
            }
        }
    }
}
