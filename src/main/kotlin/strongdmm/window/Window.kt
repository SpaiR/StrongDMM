package strongdmm.window

import imgui.enums.ImGuiCond

object Window {
    var ptr: Long = 0
    var isRunning: Boolean = true
    var isFullscreen: Boolean = false

    val _width: IntArray = IntArray(1)
    val _height: IntArray = IntArray(1)
    val _fbWidth: IntArray = IntArray(1)
    val _fbHeight: IntArray = IntArray(1)

    val windowWidth: Int
        get() = _width[0]
    val windowHeight: Int
        get() = _height[0]

    var defaultWindowCond: Int = ImGuiCond.Once

    var _resetWindows: Boolean = false
    var _toggleFullscreen: Boolean = false
}
