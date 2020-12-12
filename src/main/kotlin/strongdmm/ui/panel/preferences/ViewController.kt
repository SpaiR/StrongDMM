package strongdmm.ui.panel.preferences

import strongdmm.event.EventBus
import strongdmm.event.Reaction
import strongdmm.event.service.TriggerPreferencesService
import strongdmm.service.preferences.prefs.PreferenceBoolean
import strongdmm.service.preferences.prefs.PreferenceEnum

class ViewController(
    private val state: State
) {
    fun doSelectOption(mode: PreferenceEnum, pref: PreferenceEnum) {
        pref.getValue().data = mode.getValue().data
        savePreferences()
    }

    fun doToggleOption(pref: PreferenceBoolean) {
        pref.getValue().data = !pref.getValue().data
        savePreferences()
    }

    fun savePreferences() {
        EventBus.post(TriggerPreferencesService.SavePreferences())
    }

    fun blockApplication() {
        EventBus.post(Reaction.ApplicationBlockChanged(true))
    }

    fun checkOpenStatus() {
        if (state.checkOpenStatus && !state.isOpened.get()) {
            state.checkOpenStatus = false
            EventBus.post(Reaction.ApplicationBlockChanged(false))
        }
    }
}
