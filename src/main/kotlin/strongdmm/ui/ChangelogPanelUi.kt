package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.type.Provider
import strongdmm.event.type.ui.TriggerChangelogPanelUi
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class ChangelogPanelUi : EventConsumer {
    private val isOpened: ImBool = ImBool(false)

    private var providedChangelogText: String = ""

    init {
        consumeEvent(Provider.ChangelogControllerChangelogText::class.java, ::handleProviderChangelogControllerChangelogText)
        consumeEvent(TriggerChangelogPanelUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (!isOpened.get()) {
            return
        }

        setNextWindowPos((AppWindow.windowWidth - 800f) / 2, (AppWindow.windowHeight - 500f) / 2, AppWindow.defaultWindowCond)
        setNextWindowSize(800f, 500f, AppWindow.defaultWindowCond)

        window("Changelog", isOpened) {
            textWrapped(providedChangelogText)
        }
    }

    private fun handleProviderChangelogControllerChangelogText(event: Event<String, Unit>) {
        providedChangelogText = event.body
    }

    private fun handleOpen() {
        isOpened.set(true)
    }
}
