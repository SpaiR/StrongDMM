package strongdmm.ui.panel.screenshot

import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerScreenshotPanelUi
import strongdmm.service.settings.Settings

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Provider.SettingsServiceSettings::class.java, ::handleProviderSettingsServiceSettings)
        consumeEvent(TriggerScreenshotPanelUi.Open::class.java, ::handleOpen)
        consumeEvent(Reaction.ScreenshotTakeStarted::class.java, ::handleScreenshotTakeStarted)
        consumeEvent(Reaction.ScreenshotTakeStopped::class.java, ::handleScreenshotTakeStopped)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
    }

    private fun handleProviderSettingsServiceSettings(event: Event<Settings, Unit>) {
        state.screenshotPanelUiSettings = event.body.screenshotPanelUiSettings
        state.screenshotFilePath.set(event.body.screenshotPanelUiSettings.path)
        state.isFullMapImage = event.body.screenshotPanelUiSettings.isFullMapImage
    }

    private fun handleOpen() {
        state.isOpened.set(true)
    }

    private fun handleScreenshotTakeStarted() {
        state.isTakingScreenshot = true
    }

    private fun handleScreenshotTakeStopped() {
        state.isTakingScreenshot = false
    }

    private fun handleSelectedMapChanged() {
        state.isMapOpened = true
    }

    private fun handleSelectedMapClosed() {
        state.isMapOpened = false
    }
}
