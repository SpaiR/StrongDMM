package strongdmm.ui.panel.changelog

import imgui.type.ImBoolean
import strongdmm.util.imgui.markdown.ImMarkdown

class State {
    lateinit var providedChangelogMarkdown: ImMarkdown

    val isOpened: ImBoolean = ImBoolean(false)
}
