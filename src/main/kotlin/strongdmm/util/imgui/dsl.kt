package strongdmm.util.imgui

import imgui.ImGui
import imgui.flag.ImGuiMouseButton
import imgui.type.ImBoolean

inline fun imGuiPopupModal(name: String, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.beginPopupModal(name, imGuiWindowFlags)) {
        block()
        ImGui.endPopup()
    }
}

inline fun imGuiPopupModal(name: String, pOpen: ImBoolean, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.beginPopupModal(name, pOpen, imGuiWindowFlags)) {
        block()
        ImGui.endPopup()
    }
}

inline fun imGuiPopup(name: String, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.beginPopup(name, imGuiWindowFlags)) {
        block()
        ImGui.endPopup()
    }
}

inline fun imGuiChild(strId: String, width: Float = 0f, height: Float = 0f, border: Boolean = false, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.beginChild(strId, width, height, border, imGuiWindowFlags)) {
        block()
    }
    ImGui.endChild()
}

inline fun imGuiSelectable(label: String, selected: Boolean = false, imGuiSelectableFlags: Int = 0, sizeX: Float = 0f, sizeY: Float = 0f, block: () -> Unit) {
    if (ImGui.selectable(label, selected, imGuiSelectableFlags, sizeX, sizeY)) {
        block()
    }
}

inline fun imGuiButton(label: String, width: Float = 0f, height: Float = 0f, block: () -> Unit) {
    if (ImGui.button(label, width, height)) {
        block()
    }
}

inline fun imGuiSmallButton(label: String, block: () -> Unit) {
    if (ImGui.smallButton(label)) {
        block()
    }
}

inline fun imGuiBegin(title: String, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.begin(title, imGuiWindowFlags)) {
        block()
    }
    ImGui.end()
}

inline fun imGuiBegin(title: String, pOpen: ImBoolean, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.begin(title, pOpen, imGuiWindowFlags)) {
        block()
    }
    ImGui.end()
}

inline fun imGuiMainMenuBar(block: () -> Unit) {
    if (ImGui.beginMainMenuBar()) {
        block()
        ImGui.endMainMenuBar()
    }
}

inline fun imGuiMenu(label: String, enabled: Boolean = true, block: () -> Unit) {
    if (ImGui.beginMenu(label, enabled)) {
        block()
        ImGui.endMenu()
    }
}

inline fun imGuiMenuItem(label: String, shortcut: String = "", selected: Boolean = false, enabled: Boolean = true, block: () -> Unit) {
    if (ImGui.menuItem(label, shortcut, selected, enabled)) {
        block()
    }
}

inline fun imGuiMenuItem(label: String, shortcut: String = "", selected: ImBoolean, enabled: Boolean = true, block: () -> Unit) {
    if (ImGui.menuItem(label, shortcut, selected, enabled)) {
        block()
    }
}

inline fun imGuiPopupContextItem(strId: String, mouse: Int = ImGuiMouseButton.Left, block: () -> Unit) {
    if (ImGui.beginPopupContextItem(strId, mouse)) {
        block()
        ImGui.endPopup()
    }
}

inline fun imGuiWithStyleColor(imGuiCol: Int, col: Int, block: () -> Unit) {
    ImGui.pushStyleColor(imGuiCol, col)
    block()
    ImGui.popStyleColor()
}

inline fun imGuiWithStyleVar(imGuiStyleVar: Int, valX: Float, valY: Float, block: () -> Unit) {
    ImGui.pushStyleVar(imGuiStyleVar, valX, valY)
    block()
    ImGui.popStyleVar()
}

inline fun imGuiWithIndent(indent: Float, block: () -> Unit) {
    ImGui.indent(indent)
    block()
    ImGui.unindent(indent)
}

inline fun imGuiCombo(label: String, previewValue: String, block: () -> Unit) {
    if (ImGui.beginCombo(label, previewValue)) {
        block()
        ImGui.endCombo()
    }
}

inline fun imGuiRadioButton(label: String, active: Boolean, block: () -> Unit) {
    if (ImGui.radioButton(label, active)) {
        block()
    }
}
