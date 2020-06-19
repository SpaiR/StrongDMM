package strongdmm.util.imgui

import imgui.ImGui
import strongdmm.window.Window

object WindowUtil {
    fun getHeightPercent(percents: Int): Float {
        return Window.windowHeight / 100f * percents
    }

    fun setNextSize(windowWidth: Float, windowHeight: Float) {
        ImGui.setNextWindowSize(windowWidth, windowHeight, Window.defaultWindowCond)
    }

    fun setNextPosAndSize(x: Float, y: Float, windowWidth: Float, windowHeight: Float) {
        ImGui.setNextWindowPos(x, y, Window.defaultWindowCond)
        ImGui.setNextWindowSize(windowWidth, windowHeight, Window.defaultWindowCond)
    }

    fun setNextPosAndSizeCentered(windowWidth: Float, windowHeight: Float) {
        ImGui.setNextWindowPos((Window.windowWidth - windowWidth) / 2, (Window.windowHeight - windowHeight) / 2, Window.defaultWindowCond)
        ImGui.setNextWindowSize(windowWidth, windowHeight, Window.defaultWindowCond)
    }
}
