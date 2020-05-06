package strongdmm.ui.panel.screenshot

import imgui.ImBool
import imgui.ImString

class State {
    val isOpened: ImBool = ImBool(false)

    var isMapOpened: Boolean = false
    var screenshotFilePath: ImString = ImString().apply { inputData.isResizable = true }
    var isFullMapImage: Boolean = true

    var isTakingScreenshot: Boolean = false
}
