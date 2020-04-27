package strongdmm.ui.panel.changelog

import imgui.ImBool

class State {
    lateinit var providedChangelogText: String

    val isOpened: ImBool = ImBool(false)
}
