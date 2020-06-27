package strongdmm.ui.panel.changelog

import imgui.type.ImBoolean

class State {
    lateinit var providedChangelogText: String

    val isOpened: ImBoolean = ImBoolean(false)
}
