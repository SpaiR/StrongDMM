package strongdmm.ui.panel.screenshot

import imgui.ImGui.*
import imgui.flag.ImGuiCol
import strongdmm.util.imgui.*
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 500f * Window.pointSize
        private val height: Float
            get() = 125f * Window.pointSize

        private const val TITLE: String = "Screenshot"
    }

    lateinit var viewController: ViewController

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        ImGuiUtil.setNextWindowCentered(width, height)

        window(TITLE, state.isOpened) {
            button("Select File...", block = viewController::doSelectFile)
            sameLine()
            inputText("##screenshot_file_path", state.screenshotFilePath)

            radioButton("Full", state.isFullMapImage, viewController::doFull)
            sameLine()
            radioButton("Selection", !state.isFullMapImage, viewController::doSelection)

            val isScreenshotDisabled = viewController.isScreenshotDisabled()

            if (isScreenshotDisabled) {
                pushStyleColor(ImGuiCol.Button, GREY32)
                pushStyleColor(ImGuiCol.ButtonActive, GREY32)
                pushStyleColor(ImGuiCol.ButtonHovered, GREY32)
            }

            button("Create", block = viewController::doCreate)

            if (isScreenshotDisabled) {
                popStyleColor(3)
            }
        }
    }
}
