package strongdmm.util.imgui

import imgui.ImGui
import imgui.ImInt
import imgui.ImString

fun setItemHoveredTooltip(text: String) {
    if (ImGui.isItemHovered()) {
        ImGui.setTooltip(text)
    }
}

fun inputText(label: String, text: ImString, placeholder: String): Boolean {
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

fun inputText(label: String, text: ImString, placeholder: String, helpText: String): Boolean {
    ImGui.beginGroup()

    val helpMarkerIndent = ImGui.calcItemWidth() - 25f
    val valueChanged = ImGui.inputText(label, text)

    if (text.length == 0) {
        ImGui.sameLine()
        ImGui.indent(5f)
        ImGui.textDisabled(placeholder)
        ImGui.unindent(5f)
    }

    ImGui.sameLine()
    ImGui.indent(helpMarkerIndent)
    ImGui.textDisabled("(?)")
    setItemHoveredTooltip(helpText)
    ImGui.unindent(helpMarkerIndent)

    ImGui.endGroup()
    return valueChanged
}

fun inputInt(label: String, v: ImInt, min: Int, max: Int) {
    if (ImGui.inputInt(label, v)) {
        if (v.get() > max) {
            v.set(max)
        } else if (v.get() < min) {
            v.set(min)
        }
    }
}

fun helpMark(helpText: String) {
    ImGui.textDisabled("(?)")
    setItemHoveredTooltip(helpText)
}
