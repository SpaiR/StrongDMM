package strongdmm.ui.panel.screenshot

import strongdmm.byond.dmm.MapArea
import strongdmm.event.EventBus
import strongdmm.event.service.*
import strongdmm.util.NfdUtil
import java.io.File

class ViewController(
    private val state: State
) {
    fun doSelectFile() {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment {
            NfdUtil.saveFile("png")?.let { file ->
                state.screenshotFilePath.set(file.absolutePath + if (file.extension != "png") ".png" else "")
            }
        })
    }

    fun doCreate() {
        if (isScreenshotDisabled()) {
            return
        }

        var mapArea: MapArea = MapArea.OUT_OF_BOUNDS_AREA

        if (state.isFullMapImage) {
            EventBus.post(TriggerMapHolderService.FetchSelectedMap {
                mapArea = MapArea(1, 1, it.maxX, it.maxY)
            })
        } else {
            EventBus.post(TriggerToolsService.FetchSelectedArea {
                mapArea = it
            })
        }

        saveScreenshotSettings()
        EventBus.post(TriggerScreenshotService.TakeScreenshot(Pair(File(state.screenshotFilePath.get()), mapArea)))
    }

    fun doFull() {
        state.isFullMapImage = true
    }

    fun doSelection() {
        state.isFullMapImage = false
    }

    fun isScreenshotDisabled(): Boolean {
        return !state.isMapOpened || state.isTakingScreenshot || state.screenshotFilePath.length == 0
    }

    private fun saveScreenshotSettings() {
        state.screenshotPanelUiSettings.path = state.screenshotFilePath.get()
        state.screenshotPanelUiSettings.isFullMapImage = state.isFullMapImage
        EventBus.post(TriggerSettingsService.SaveSettings())
    }
}
