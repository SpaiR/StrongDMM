package strongdmm.util.imgui

import imgui.internal.ImGui
import imgui.flag.ImGuiStyleVar
import imgui.internal.flag.ImGuiItemFlags
import strongdmm.application.window.Window

object ImGuiUtil {
    fun setNextWindowCentered(windowWidth: Float, windowHeight: Float, imGuiCond: Int = Window.windowCond) {
        ImGui.setNextWindowPos((Window.windowWidth - windowWidth) / 2, (Window.windowHeight - windowHeight) / 2, imGuiCond)
        ImGui.setNextWindowSize(windowWidth, windowHeight, imGuiCond)
    }

    fun pushDisabledItem() {
        ImGui.pushItemFlag(ImGuiItemFlags.Disabled, true)
        ImGui.pushStyleVar(ImGuiStyleVar.Alpha, ImGui.getStyle().alpha * .5f)
    }

    fun popDisabledItem() {
        ImGui.popStyleVar()
        ImGui.popItemFlag()
    }
}
