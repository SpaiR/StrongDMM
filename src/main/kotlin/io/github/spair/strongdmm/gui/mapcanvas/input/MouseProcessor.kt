package io.github.spair.strongdmm.gui.mapcanvas.input

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.mapcanvas.Frame
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasController
import io.github.spair.strongdmm.gui.mapcanvas.openTilePopup
import io.github.spair.strongdmm.logic.map.OUT_OF_BOUNDS
import org.lwjgl.input.Mouse

object MouseProcessor {

    private const val LMB = 0
    private const val RMB = 1

    private val mapCanvasCtrl by diInstance<MapCanvasController>()

    fun fire() {
        mapCanvasCtrl.updateMouseMapPosition()

        if (Mouse.isButtonDown(LMB)) {
            mapCanvasCtrl.updateViewAndMapOffset()
        }

        Mouse.getDWheel().takeIf { it != 0 }?.let {
            mapCanvasCtrl.updateZoom(it > 0)
        }

        while (Mouse.next()) {
            if (Mouse.getEventButtonState()) {
                if (mapCanvasCtrl.view.tryCloseTilePopup() && Mouse.getEventButton() != RMB) {
                    continue
                }

                if (Mouse.getEventButton() == RMB) {
                    mapCanvasCtrl.openTilePopup()
                }
            }
        }
    }

    private fun MapCanvasController.updateViewAndMapOffset() {
        val x = Mouse.getDX()
        val y = Mouse.getDY()

        val xViewOffNew = xViewOff + x * viewZoom
        val yViewOffNew = yViewOff + y * viewZoom

        if (xViewOffNew != xViewOff || yViewOffNew != yViewOff) {
            xViewOff = xViewOffNew
            yViewOff = yViewOffNew

            updateMapOffset()
            Frame.update()
        }
    }

    private fun MapCanvasController.updateZoom(isZoomIn: Boolean) {
        mapCanvasCtrl.view.tryCloseTilePopup()

        if ((!isZoomIn && currZoom - 1 < maxZoomOut) || (isZoomIn && currZoom + 1 > maxZoomIn)) {
            return
        }

        currZoom += if (isZoomIn) 1 else -1


        if (isZoomIn) {
            viewZoom /= zoomFactor
            xViewOff -= Mouse.getX() * viewZoom / 2
            yViewOff -= Mouse.getY() * viewZoom / 2
        } else {
            xViewOff += Mouse.getX() * viewZoom / 2
            yViewOff += Mouse.getY() * viewZoom / 2
            viewZoom *= zoomFactor
        }

        updateMapOffset()
        Frame.update()
    }

    private fun MapCanvasController.updateMapOffset() {
        xMapOff = (-xViewOff / iconSize + 0.5f).toInt()
        yMapOff = (-yViewOff / iconSize + 0.5f).toInt()
    }

    private fun MapCanvasController.updateMouseMapPosition() {
        var xMouseMapNew = (Mouse.getX() * viewZoom - xViewOff).toInt() / iconSize + 1
        var yMouseMapNew = (Mouse.getY() * viewZoom - yViewOff).toInt() / iconSize + 1

        xMouseMapNew = if (xMouseMapNew < 1 || xMouseMapNew > selectedMap!!.maxX) OUT_OF_BOUNDS else xMouseMapNew
        yMouseMapNew = if (yMouseMapNew < 1 || yMouseMapNew > selectedMap!!.maxY) OUT_OF_BOUNDS else yMouseMapNew

        if (xMouseMapNew != xMouseMap || yMouseMapNew != yMouseMap) {
            xMouseMap = xMouseMapNew
            yMouseMap = yMouseMapNew
            Frame.update()
        }
    }
}
