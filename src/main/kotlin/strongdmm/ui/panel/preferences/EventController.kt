package strongdmm.ui.panel.preferences

import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.ui.TriggerPreferencesPanelUi
import strongdmm.service.preferences.Preferences

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Provider.PreferencesServicePreferences::class.java, ::handleProviderPreferencesServicePreferences)
        consumeEvent(TriggerPreferencesPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleProviderPreferencesServicePreferences(event: Event<Preferences, Unit>) {
        state.providedPreferences = event.body
    }

    private fun handleOpen() {
        state.isOpened.set(true)
        state.isDoOpen = true
    }
}
