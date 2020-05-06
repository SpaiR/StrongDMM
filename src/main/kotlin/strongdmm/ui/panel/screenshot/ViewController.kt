package strongdmm.ui.panel.screenshot

import strongdmm.byond.dmm.MapArea
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.event.type.service.TriggerScreenshotService
import strongdmm.event.type.service.TriggerToolsService
import strongdmm.util.NfdUtil
import java.io.File

class ViewController(
    private val state: State
) : EventHandler {
    fun doSelectFile() {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
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
            sendEvent(TriggerMapHolderService.FetchSelectedMap {
                mapArea = MapArea(1, 1, it.maxX, it.maxY)
            })
        } else {
            sendEvent(TriggerToolsService.FetchSelectedArea {
                mapArea = it
            })
        }

        sendEvent(TriggerScreenshotService.TakeScreenshot(Pair(File(state.screenshotFilePath.get()), mapArea)))
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
}
