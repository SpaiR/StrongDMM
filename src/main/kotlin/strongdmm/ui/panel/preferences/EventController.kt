package strongdmm.ui.panel.preferences

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ProviderPreferencesService
import strongdmm.event.type.ui.TriggerPreferencesPanelUi
import strongdmm.service.preferences.Preferences

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ProviderPreferencesService.Preferences::class.java, ::handleProviderPreferences)
        EventBus.sign(TriggerPreferencesPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleProviderPreferences(event: Event<Preferences, Unit>) {
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
