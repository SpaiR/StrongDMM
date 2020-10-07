package strongdmm.ui.panel.preferences

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Provider
import strongdmm.event.type.ui.TriggerPreferencesPanelUi
import strongdmm.service.preferences.Preferences

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Provider.PreferencesServicePreferences::class.java, ::handleProviderPreferencesServicePreferences)
        EventBus.sign(TriggerPreferencesPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleProviderPreferencesServicePreferences(event: Event<Preferences, Unit>) {
        state.providedPreferences = event.body

        state.providedPreferences.rawValues.forEach { preference ->
            state.preferencesByGroups.getOrPut(preference.getGroup()) { mutableListOf() }.add(preference)
        }
    }

    private fun handleOpen() {
        state.isOpened.set(true)
        state.isDoOpen = true
    }
}
