package strongdmm.service.changelog

import strongdmm.application.PostInitialize
import strongdmm.application.Service
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ProviderChangelogService
import strongdmm.event.type.service.ProviderSettingsService
import strongdmm.event.type.service.TriggerSettingsService
import strongdmm.event.type.ui.TriggerChangelogPanelUi
import strongdmm.service.settings.Settings
import strongdmm.util.imgui.markdown.ImGuiMarkdown

class ChangelogService : Service, PostInitialize {
    private val rawChangelogText: String = this::class.java.classLoader.getResourceAsStream("CHANGELOG.md")!!.use {
        it.readAllBytes().toString(Charsets.UTF_8)
    }

    init {
        EventBus.sign(ProviderSettingsService.Settings::class.java, ::handleProviderSettings)
    }

    override fun postInit() {
        EventBus.post(ProviderChangelogService.ChangelogMarkdown(ImGuiMarkdown.parse(rawChangelogText)))
    }

    private fun handleProviderSettings(event: Event<Settings, Unit>) {
        val changelogServiceSettings = event.body.changelogServiceSettings
        val currentHash = rawChangelogText.hashCode()

        if (changelogServiceSettings.lastOpenChangelogHash != currentHash) {
            EventBus.post(TriggerChangelogPanelUi.Open())
            changelogServiceSettings.lastOpenChangelogHash = currentHash
            EventBus.post(TriggerSettingsService.SaveSettings())
        }
    }
}
