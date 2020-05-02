package strongdmm.service.preferences

import com.google.gson.Gson
import strongdmm.StrongDMM
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.service.TriggerPreferencesService
import java.io.File

class PreferencesService : EventHandler {
    companion object {
        private val preferencesConfig: File = File(StrongDMM.homeDir.toFile(), "preferences.json")
    }

    private lateinit var preferences: Preferences

    init {
        consumeEvent(TriggerPreferencesService.SavePreferences::class.java, ::handleSavePreferences)
    }

    fun postInit() {
        ensurePreferencesConfigExists()
        readPreferencesConfig()

        sendEvent(Provider.PreferencesControllerPreferences(preferences))
    }

    private fun ensurePreferencesConfigExists() {
        if (preferencesConfig.createNewFile()) {
            preferencesConfig.writeText(Gson().toJson(Preferences()))
        }
    }

    private fun readPreferencesConfig() {
        preferencesConfig.reader().use {
            preferences = Gson().fromJson(it, Preferences::class.java)
        }
    }

    private fun writePreferencesConfig() {
        preferencesConfig.writeText(Gson().toJson(preferences))
    }

    private fun handleSavePreferences() {
        writePreferencesConfig()
    }
}
