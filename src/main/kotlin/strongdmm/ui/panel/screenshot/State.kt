package strongdmm.ui.panel.screenshot

import imgui.type.ImBoolean
import imgui.type.ImString
import strongdmm.ui.panel.screenshot.model.ScreenshotPanelUiSettings

class State {
    lateinit var screenshotPanelUiSettings: ScreenshotPanelUiSettings

    val isOpened: ImBoolean = ImBoolean(false)

    var isMapOpened: Boolean = false
    var screenshotFilePath: ImString = ImString().apply { inputData.isResizable = true }
    var isFullMapImage: Boolean = true

    var isTakingScreenshot: Boolean = false
}
