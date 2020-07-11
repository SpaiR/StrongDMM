package strongdmm.service.settings

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import strongdmm.PostInitialize
import strongdmm.Service
import strongdmm.StrongDMM
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.service.TriggerSettingsService
import java.io.File

class SettingsService : Service, PostInitialize, EventHandler {
    companion object {
        private val settingsConfig: File = File(StrongDMM.homeDir.toFile(), "settings.json")
    }

    private lateinit var settings: Settings

    private val objectMapper: ObjectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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
            writeSettingsConfig(Settings())
        }
    }

    private fun readSettingsConfig() {
        try {
            settings = objectMapper.readValue(settingsConfig, Settings::class.java)
        } catch (e: Exception) {
            writeSettingsConfig(Settings())
            readSettingsConfig()
        }
    }

    private fun writeSettingsConfig(settings: Settings) {
        objectMapper.writeValue(settingsConfig, settings)
    }

    private fun handleSaveSettings() {
        writeSettingsConfig(settings)
    }
}
