package strongdmm.ui.panel.about

import imgui.type.ImBoolean

class State {
    companion object {
        private const val ABOUT_FILE: String = "about.txt"
    }

    val isOpened: ImBoolean = ImBoolean(false)

    val aboutText: String = this::class.java.classLoader.getResourceAsStream(ABOUT_FILE).use {
        it!!.readBytes().toString(Charsets.UTF_8)
    }
}
