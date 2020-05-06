package strongdmm.ui.panel.screenshot

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import strongdmm.util.imgui.*

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 500f
        private const val HEIGHT: Float = 125f

        private const val TITLE: String = "Screenshot"
    }

    lateinit var viewController: ViewController

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        WindowUtil.setNextPosAndSizeCentered(WIDTH, HEIGHT)

        window(TITLE, state.isOpened) {
            button("Select File", block = viewController::doSelectFile)
            sameLine()
            inputText("##screenshot_file_path", state.screenshotFilePath)

            radioButton("Full", state.isFullMapImage, viewController::doFull)
            sameLine()
            radioButton("Selection", !state.isFullMapImage, viewController::doSelection)

            newLine()

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
