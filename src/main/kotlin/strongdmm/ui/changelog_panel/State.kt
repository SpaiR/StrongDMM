package strongdmm.ui.changelog_panel

import imgui.ImBool

class State {
    lateinit var providedChangelogText: String

    val isOpened: ImBool = ImBool(false)
}
