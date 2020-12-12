package strongdmm.service.settings

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import strongdmm.StrongDMM
import strongdmm.application.PostInitialize
import strongdmm.application.Service
import strongdmm.event.EventBus
import strongdmm.event.service.ProviderSettingsService
import strongdmm.event.service.TriggerSettingsService
import java.io.File

class SettingsService : Service, PostInitialize {
    companion object {
        private val settingsConfig: File = File(StrongDMM.homeDir.toFile(), "settings.json")
    }

    private lateinit var settings: Settings

    private val objectMapper: ObjectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    init {
        EventBus.sign(TriggerSettingsService.SaveSettings::class.java, ::handleSaveSettings)
    }

    override fun postInit() {
        ensureSettingsConfigExists()
        readSettingsConfig()

        EventBus.post(ProviderSettingsService.Settings(settings))
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
