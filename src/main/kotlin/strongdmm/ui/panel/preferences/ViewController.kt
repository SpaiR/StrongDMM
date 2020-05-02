package strongdmm.ui.panel.preferences

import imgui.ImBool
import strongdmm.service.preferences.Selectable
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerPreferencesController

class ViewController(
    private val state: State
) : EventHandler {
    inline fun doSelectOption(mode: Selectable, action: (Selectable) -> Unit) {
        action(mode)
        savePreferences()
    }

    fun doToggleOption(option: ImBool) {
        option.set(!option.get())
        savePreferences()
    }

    fun savePreferences() {
        sendEvent(TriggerPreferencesController.SavePreferences())
    }

    fun blockApplication() {
        sendEvent(Reaction.ApplicationBlockChanged(true))
    }

    fun checkOpenStatus() {
        if (state.checkOpenStatus && !state.isOpened.get()) {
            state.checkOpenStatus = false
            sendEvent(Reaction.ApplicationBlockChanged(false))
        }
    }
}
