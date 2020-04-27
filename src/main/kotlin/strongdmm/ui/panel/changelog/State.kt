package strongdmm.ui.panel.changelog_panel

import imgui.ImBool

class State {
    lateinit var providedChangelogText: String

    val isOpened: ImBool = ImBool(false)
}
