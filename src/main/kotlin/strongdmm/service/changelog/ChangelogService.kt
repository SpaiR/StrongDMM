package strongdmm.service.changelog

import strongdmm.application.PostInitialize
import strongdmm.application.Service
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Provider
import strongdmm.event.type.service.TriggerSettingsService
import strongdmm.event.type.ui.TriggerChangelogPanelUi
import strongdmm.service.settings.Settings
import strongdmm.util.imgui.markdown.ImGuiMarkdown

class ChangelogService : Service, PostInitialize {
    private val rawChangelogText: String = this::class.java.classLoader.getResourceAsStream("CHANGELOG.md")!!.use {
        it.readAllBytes().toString(Charsets.UTF_8)
    }

    init {
        EventBus.sign(Provider.SettingsServiceSettings::class.java, ::handleProviderSettingsServiceSettings)
    }

    override fun postInit() {
        EventBus.post(Provider.ChangelogServiceChangelogMarkdown(ImGuiMarkdown.parse(rawChangelogText)))
    }

    private fun handleProviderSettingsServiceSettings(event: Event<Settings, Unit>) {
        val changelogServiceSettings = event.body.changelogServiceSettings
        val currentHash = rawChangelogText.hashCode()

        if (changelogServiceSettings.lastOpenChangelogHash != currentHash) {
            EventBus.post(TriggerChangelogPanelUi.Open())
            changelogServiceSettings.lastOpenChangelogHash = currentHash
            EventBus.post(TriggerSettingsService.SaveSettings())
        }
    }
}
