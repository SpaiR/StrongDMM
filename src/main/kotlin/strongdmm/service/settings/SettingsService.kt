package strongdmm.service.settings

import com.google.gson.Gson
import strongdmm.PostInitialize
import strongdmm.Service
import strongdmm.StrongDMM
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.service.TriggerSettingsService
import strongdmm.service.preferences.Preferences
import java.io.File

class SettingsService : Service, PostInitialize, EventHandler {
    companion object {
        private val settingsConfig: File = File(StrongDMM.homeDir.toFile(), "settings.json")
    }

    private lateinit var settings: Settings

    init {
        consumeEvent(TriggerSettingsService.SaveSettings::class.java, ::handleSaveSettings)
    }

    override fun postInit() {
        ensureSettingsConfigExists()
        readSettingsConfig()

        sendEvent(Provider.SettingsServiceSettings(settings))
    }

    private fun ensureSettingsConfigExists() {
        if (settingsConfig.createNewFile()) {
            settingsConfig.writeText(Gson().toJson(Preferences()))
        }
    }

    private fun readSettingsConfig() {
        settingsConfig.reader().use {
            settings = Gson().fromJson(it, Settings::class.java)
        }
    }

    private fun writeSettingsConfig() {
        settingsConfig.writeText(Gson().toJson(settings))
    }

    private fun handleSaveSettings() {
        writeSettingsConfig()
    }
}
