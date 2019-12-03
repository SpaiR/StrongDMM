package strongdmm.util.imgui

import imgui.ImGui
import strongdmm.util.LMB

inline fun Any.itemHovered(block: () -> Unit) {
    if (ImGui.isItemHovered()) {
        block()
    }
}

inline fun Boolean.itemAction(block: () -> Unit) {
    if (this) {
        block()
    }
}

inline fun Any.itemClicked(mouse: Int = LMB, block: () -> Unit) {
    if (ImGui.isItemClicked(mouse)) {
        block()
    }
}

inline fun Any.with(block: () -> Unit) {
    block()
}
