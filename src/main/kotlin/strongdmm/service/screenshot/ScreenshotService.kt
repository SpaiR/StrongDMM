package strongdmm.service.screenshot

import strongdmm.Service
import strongdmm.byond.dmm.MapArea
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerScreenshotService
import strongdmm.service.frame.FrameMesh
import strongdmm.util.DEFAULT_ICON_SIZE
import strongdmm.util.imgui.col32argb
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import kotlin.concurrent.thread

class ScreenshotService : Service, EventHandler {
    private val screenshotRenderer = ScreenshotRenderer()

    init {
        consumeEvent(Provider.FrameServiceComposedFrame::class.java, ::handleProviderFrameServiceComposedFrame)
        consumeEvent(TriggerScreenshotService.TakeScreenshot::class.java, ::handleTakeScreenshot)
    }

    private fun saveImageBytesToPng(imageBytes: ByteBuffer, width: Int, height: Int, pngFile: File) {
        thread(start = true) {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val pos = 4 * ((height - 1 - y) * width + x)
                    val r = imageBytes[pos].toInt() and 0xff
                    val g = imageBytes[pos + 1].toInt() and 0xff
                    val b = imageBytes[pos + 2].toInt() and 0xff
                    val a = imageBytes[pos + 3].toInt() and 0xff
                    if (a != 0) {
                        image.setRGB(x, y, col32argb(r, g, b, a))
                    }
                }
            }

            ImageIO.write(image, "png", pngFile)
            System.gc()

            sendEvent(Reaction.ScreenshotTakeStopped())
        }
    }

    private fun handleTakeScreenshot(event: Event<Pair<File, MapArea>, Unit>) {
        val (file, mapArea) = event.body

        try {
            file.createNewFile()
        } catch (e: IOException) {
            return
        }

        sendEvent(Reaction.ScreenshotTakeStarted())

        var iconSize = DEFAULT_ICON_SIZE

        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            iconSize = it.getWorldIconSize()
        })

        val maxX = mapArea.x2 - mapArea.x1
        val maxY = mapArea.y2 - mapArea.y1

        val width = iconSize * (maxX + 1)
        val height = iconSize * (maxY + 1)

        val xShift = (mapArea.x1 - 1) * iconSize * -1f
        val yShift = (mapArea.y1 - 1) * iconSize * -1f

        val imageBytes = screenshotRenderer.render(width, height, mapArea, xShift, yShift)

        saveImageBytesToPng(imageBytes, width, height, file)
    }

    private fun handleProviderFrameServiceComposedFrame(event: Event<List<FrameMesh>, Unit>) {
        screenshotRenderer.providedFrameMeshes = event.body
    }
}
