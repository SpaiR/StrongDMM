package strongdmm.ui.panel.about

import imgui.ImBool

class State {
    companion object {
        private const val ABOUT_FILE: String = "about.txt"
    }

    val isOpened: ImBool = ImBool(false)

    val aboutText: String = this::class.java.classLoader.getResourceAsStream(ABOUT_FILE).use {
        it!!.readBytes().toString(Charsets.UTF_8)
    }
}
