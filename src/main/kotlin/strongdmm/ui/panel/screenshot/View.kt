package strongdmm.ui.panel.screenshot

import imgui.ImGui
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.imGuiButton
import strongdmm.util.imgui.imGuiRadioButton
import strongdmm.util.imgui.imGuiBegin
import strongdmm.application.window.Window

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

        imGuiBegin(TITLE, state.isOpened) {
            imGuiButton("Select File...", block = viewController::doSelectFile)
            ImGui.sameLine()
            ImGui.inputText("##screenshot_file_path", state.screenshotFilePath)

            imGuiRadioButton("Full", state.isFullMapImage, viewController::doFull)
            ImGui.sameLine()
            imGuiRadioButton("Selection", !state.isFullMapImage, viewController::doSelection)

            val isScreenshotDisabled = viewController.isScreenshotDisabled()

            if (isScreenshotDisabled) {
                ImGuiUtil.pushDisabledItem()
            }

            imGuiButton("Create", block = viewController::doCreate)

            if (isScreenshotDisabled) {
                ImGuiUtil.popDisabledItem()
            }
        }
    }
}
