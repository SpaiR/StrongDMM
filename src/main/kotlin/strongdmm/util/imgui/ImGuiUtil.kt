package strongdmm.util.imgui

import imgui.ImGui
import imgui.flag.ImGuiCol
import strongdmm.window.Window

object ImGuiUtil {
    fun setNextWindowCentered(windowWidth: Float, windowHeight: Float, imGuiCond: Int = Window.windowCond) {
        ImGui.setNextWindowPos((Window.windowWidth - windowWidth) / 2, (Window.windowHeight - windowHeight) / 2, imGuiCond)
        ImGui.setNextWindowSize(windowWidth, windowHeight, imGuiCond)
    }

    fun pushDisabledButtonStyle() {
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ImGui.getColorU32(ImGuiCol.Button, .25f))
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, ImGui.getColorU32(ImGuiCol.Button, .25f))
        ImGui.pushStyleColor(ImGuiCol.Button, ImGui.getColorU32(ImGuiCol.Button, .25f))
        ImGui.pushStyleColor(ImGuiCol.Text, COLOR_DIMGREY)
    }

    fun popDisabledButtonStyle() {
        ImGui.popStyleColor(4)
    }
}
