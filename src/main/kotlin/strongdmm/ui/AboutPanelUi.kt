package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import strongdmm.event.EventConsumer
import strongdmm.event.type.ui.TriggerAboutPanelUi
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class AboutPanelUi : EventConsumer {
    private val isOpened: ImBool = ImBool(false)
    private val aboutText: String = this::class.java.classLoader.getResourceAsStream("about.txt").use {
        it!!.readAllBytes().toString(Charsets.UTF_8)
    }

    init {
        consumeEvent(TriggerAboutPanelUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (!isOpened.get()) {
            return
        }

        setNextWindowPos((AppWindow.windowWidth - 550f) / 2, (AppWindow.windowHeight - 150f) / 2, AppWindow.defaultWindowCond)
        setNextWindowSize(550f, 150f, AppWindow.defaultWindowCond)

        window("About", isOpened) {
            textWrapped(aboutText)
        }
    }

    private fun handleOpen() {
        isOpened.set(true)
    }
}
