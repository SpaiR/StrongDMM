package strongdmm.ui.panel.screenshot

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ProviderSettingsService
import strongdmm.event.type.service.ReactionMapHolderService
import strongdmm.event.type.service.ReactionScreenshotService
import strongdmm.event.type.ui.TriggerScreenshotPanelUi
import strongdmm.service.settings.Settings

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ProviderSettingsService.Settings::class.java, ::handleProviderSettings)
        EventBus.sign(TriggerScreenshotPanelUi.Open::class.java, ::handleOpen)
        EventBus.sign(ReactionScreenshotService.ScreenshotTakeStarted::class.java, ::handleScreenshotTakeStarted)
        EventBus.sign(ReactionScreenshotService.ScreenshotTakeStopped::class.java, ::handleScreenshotTakeStopped)
        EventBus.sign(ReactionMapHolderService.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
    }

    private fun handleProviderSettings(event: Event<Settings, Unit>) {
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
