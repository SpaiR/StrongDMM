package strongdmm.util.imgui

import imgui.ImGui
import strongdmm.window.Window

object ImGuiUtil {
    fun setNextWindowCentered(windowWidth: Float, windowHeight: Float, imGuiCond: Int = Window.windowCond) {
        ImGui.setNextWindowPos((Window.windowWidth - windowWidth) / 2, (Window.windowHeight - windowHeight) / 2, imGuiCond)
        ImGui.setNextWindowSize(windowWidth, windowHeight, imGuiCond)
    }
}
