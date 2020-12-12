package strongdmm.service.preferences

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import strongdmm.StrongDMM
import strongdmm.application.PostInitialize
import strongdmm.application.Service
import strongdmm.event.EventBus
import strongdmm.event.service.ProviderPreferencesService
import strongdmm.event.service.TriggerPreferencesService
import strongdmm.service.preferences.prefs.Preference
import java.io.File

class PreferencesService : Service, PostInitialize {
    companion object {
        private val preferencesConfig: File = File(StrongDMM.homeDir.toFile(), "preferences.json")
    }

    private lateinit var preferences: Preferences
    private val objectMapper: ObjectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    init {
        EventBus.sign(TriggerPreferencesService.SavePreferences::class.java, ::handleSavePreferences)
    }

    override fun postInit() {
        ensurePreferencesConfigExists()
        readPreferencesConfig()
        applyModifiedPreferences()

        EventBus.post(ProviderPreferencesService.Preferences(preferences))
    }

    private fun ensurePreferencesConfigExists() {
        if (preferencesConfig.createNewFile()) {
            writePreferencesConfig(Preferences())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readPreferencesConfig() {
        try {
            preferences = objectMapper.readValue(preferencesConfig, Preferences::class.java)
            preferences.rawValues = preferences::class.java.declaredFields.mapNotNull {
                it.isAccessible = true
                it.get(preferences) as? Preference<Any>
            }
        } catch (e: Exception) {
            writePreferencesConfig(Preferences())
            readPreferencesConfig()
        }
    }

    private fun writePreferencesConfig(preferences: Preferences) {
        objectMapper.writeValue(preferencesConfig, preferences)
    }

    private fun handleSavePreferences() {
        applyModifiedPreferences()
        writePreferencesConfig(preferences)
    }

    private fun applyModifiedPreferences() {
        preferences.rawValues.forEach(Preference<Any>::applyModify)
    }
}
