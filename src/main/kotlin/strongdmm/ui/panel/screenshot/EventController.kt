package strongdmm.ui.panel.screenshot

import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerScreenshotPanelUi

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(TriggerScreenshotPanelUi.Open::class.java, ::handleOpen)
        consumeEvent(Reaction.ScreenshotTakeStarted::class.java, ::handleScreenshotTakeStarted)
        consumeEvent(Reaction.ScreenshotTakeStopped::class.java, ::handleScreenshotTakeStopped)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
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
