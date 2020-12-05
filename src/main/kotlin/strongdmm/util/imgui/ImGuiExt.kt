package strongdmm.util.imgui

import imgui.ImGui
import imgui.flag.ImGuiMouseButton
import imgui.type.ImInt
import imgui.type.ImString
import strongdmm.util.imgui.ext.WindowButton

object ImGuiExt {
    private val windowButton = WindowButton()

    fun windowButton(btnTxt: String, mouseBtn: Int = ImGuiMouseButton.Left, action: () -> Unit) = windowButton.render(btnTxt, mouseBtn, action)

    fun setItemHoveredTooltip(text: String) {
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(text)
        }
    }

    fun inputTextPlaceholder(label: String, text: ImString, placeholder: String): Boolean {
        ImGui.beginGroup()
        val valueChanged = ImGui.inputText(label, text)

        if (text.length == 0) {
            ImGui.sameLine()
            ImGui.indent(5f)
            ImGui.textDisabled(placeholder)
            ImGui.unindent(5f)
        }

        ImGui.endGroup()
        return valueChanged
    }

    fun inputIntClamp(label: String, v: ImInt, min: Int, max: Int, step: Int = 1, stepFast: Int = 10): Boolean {
        if (ImGui.inputInt(label, v, step, stepFast)) {
            if (v.get() > max) {
                v.set(max)
            } else if (v.get() < min) {
                v.set(min)
            }

            return true
        }

        return false
    }

    fun helpMark(helpText: String) {
        ImGui.textDisabled(ImGuiIconFA.QUESTION_CIRCLE)
        setItemHoveredTooltip(helpText)
    }
}
