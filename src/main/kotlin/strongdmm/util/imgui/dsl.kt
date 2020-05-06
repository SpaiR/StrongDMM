package strongdmm.util.imgui

import imgui.ImBool
import imgui.ImGui
import imgui.enums.ImGuiMouseButton

inline fun popupModal(name: String, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.beginPopupModal(name, imGuiWindowFlags)) {
        block()
        ImGui.endPopup()
    }
}

inline fun popupModal(name: String, pOpen: ImBool, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.beginPopupModal(name, pOpen, imGuiWindowFlags)) {
        block()
        ImGui.endPopup()
    }
}

inline fun popup(name: String, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.beginPopup(name, imGuiWindowFlags)) {
        block()
        ImGui.endPopup()
    }
}

inline fun child(strId: String, width: Float = 0f, height: Float = 0f, border: Boolean = false, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.beginChild(strId, width, height, border, imGuiWindowFlags)) {
        block()
    }
    ImGui.endChild()
}

inline fun selectable(label: String, selected: ImBool, imGuiSelectableFlags: Int = 0, sizeX: Float = 0f, sizeY: Float = 0f, block: () -> Unit) {
    if (ImGui.selectable(label, selected, imGuiSelectableFlags, sizeX, sizeY)) {
        block()
    }
}

inline fun selectable(label: String, selected: Boolean = false, imGuiSelectableFlags: Int = 0, sizeX: Float = 0f, sizeY: Float = 0f, block: () -> Unit) {
    if (ImGui.selectable(label, selected, imGuiSelectableFlags, sizeX, sizeY)) {
        block()
    }
}

inline fun button(label: String, width: Float = 0f, height: Float = 0f, block: () -> Unit) {
    if (ImGui.button(label, width, height)) {
        block()
    }
}

inline fun smallButton(label: String, block: () -> Unit) {
    if (ImGui.smallButton(label)) {
        block()
    }
}

inline fun window(title: String, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.begin(title, imGuiWindowFlags)) {
        block()
    }
    ImGui.end()
}

inline fun window(title: String, pOpen: ImBool, imGuiWindowFlags: Int = 0, block: () -> Unit) {
    if (ImGui.begin(title, pOpen, imGuiWindowFlags)) {
        block()
    }
    ImGui.end()
}

inline fun mainMenuBar(block: () -> Unit) {
    if (ImGui.beginMainMenuBar()) {
        block()
        ImGui.endMainMenuBar()
    }
}

inline fun menuBar(block: () -> Unit) {
    if (ImGui.beginMenuBar()) {
        block()
        ImGui.endMenuBar()
    }
}

inline fun menu(label: String, enabled: Boolean = true, block: () -> Unit) {
    if (ImGui.beginMenu(label, enabled)) {
        block()
        ImGui.endMenu()
    }
}

inline fun menuItem(label: String, shortcut: String = "", selected: Boolean = false, enabled: Boolean = true, block: () -> Unit) {
    if (ImGui.menuItem(label, shortcut, selected, enabled)) {
        block()
    }
}

inline fun menuItem(label: String, shortcut: String = "", selected: ImBool, enabled: Boolean = true, block: () -> Unit) {
    if (ImGui.menuItem(label, shortcut, selected, enabled)) {
        block()
    }
}

inline fun popupContextItem(strId: String, mouse: Int = ImGuiMouseButton.Left, block: () -> Unit) {
    if (ImGui.beginPopupContextItem(strId, mouse)) {
        block()
        ImGui.endPopup()
    }
}

inline fun withStyleColor(imGuiCol: Int, col: Int, block: () -> Unit) {
    ImGui.pushStyleColor(imGuiCol, col)
    block()
    ImGui.popStyleColor()
}

inline fun withStyleVar(imGuiStyleVar: Int, valX: Float, valY: Float, block: () -> Unit) {
    ImGui.pushStyleVar(imGuiStyleVar, valX, valY)
    block()
    ImGui.popStyleVar()
}

inline fun withIndent(indent: Float, block: () -> Unit) {
    ImGui.indent(indent)
    block()
    ImGui.unindent(indent)
}

inline fun combo(label: String, previewValue: String, block: () -> Unit) {
    if (ImGui.beginCombo(label, previewValue)) {
        block()
        ImGui.endCombo()
    }
}

inline fun radioButton(label: String, active: Boolean, block: () -> Unit) {
    if (ImGui.radioButton(label, active)) {
        block()
    }
}
