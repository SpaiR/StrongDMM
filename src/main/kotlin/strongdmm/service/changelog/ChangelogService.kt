package strongdmm.service.changelog

import strongdmm.PostInitialize
import strongdmm.Service
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.service.TriggerSettingsService
import strongdmm.event.type.ui.TriggerChangelogPanelUi
import strongdmm.service.settings.Settings
import strongdmm.util.imgui.markdown.ImGuiMarkdown

class ChangelogService : Service, EventHandler, PostInitialize {
    private val rawChangelogText: String = this::class.java.classLoader.getResourceAsStream("CHANGELOG.md")!!.use {
        it.readAllBytes().toString(Charsets.UTF_8)
    }

    init {
        consumeEvent(Provider.SettingsServiceSettings::class.java, ::handleProviderSettingsServiceSettings)
    }

    override fun postInit() {
        sendEvent(Provider.ChangelogServiceChangelogMarkdown(ImGuiMarkdown.parse(rawChangelogText)))
    }

    private fun handleProviderSettingsServiceSettings(event: Event<Settings, Unit>) {
        val changelogServiceSettings = event.body.changelogServiceSettings
        val currentHash = rawChangelogText.hashCode()

        if (changelogServiceSettings.lastOpenChangelogHash != currentHash) {
            sendEvent(TriggerChangelogPanelUi.Open())
            changelogServiceSettings.lastOpenChangelogHash = currentHash
            sendEvent(TriggerSettingsService.SaveSettings())
        }
    }
}
