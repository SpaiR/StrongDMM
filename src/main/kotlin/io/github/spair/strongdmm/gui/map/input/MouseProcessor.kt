package io.github.spair.strongdmm.gui.map.input

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.MapPipeline
import io.github.spair.strongdmm.gui.map.placeItemOnMap
import io.github.spair.strongdmm.gui.map.openTilePopup
import io.github.spair.strongdmm.logic.map.OUT_OF_BOUNDS
import org.lwjgl.input.Mouse

// Class to consume and process input from mouse.
// Class handles input only in case, when map canvas is in focus.
// For other cases (common swing flow) event driven developments is used.
object MouseProcessor {

    lateinit var mapPipeline: MapPipeline

    private const val LMB = 0
    private const val RMB = 1
    private const val MMB = 2

    fun fire() {
        mapPipeline.updateMousePosition()

        if (Mouse.isButtonDown(MMB)) {
            mapPipeline.updateViewAndMapOffset()
        }

        Mouse.getDWheel().takeIf { it != 0 }?.let {
            mapPipeline.updateZoom(it > 0)
        }

        // clicks handling
        while (Mouse.next()) {
            if (Mouse.getEventButtonState()) {
                if (mapPipeline.view.tryCloseTilePopup() && Mouse.getEventButton() != RMB) {
                    continue
                }

                when (Mouse.getEventButton()) {
                    LMB -> {
                        if (KeyboardProcessor.isCtrlDown() && KeyboardProcessor.isShiftDown()) {
                            mapPipeline.selectItem = true
                            Frame.update()
                        } else {
                            mapPipeline.placeItemOnMap()
                        }
                    }
                    RMB -> mapPipeline.openTilePopup()
                }
            }
        }
    }

    private fun MapPipeline.updateViewAndMapOffset() {
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

    private fun MapPipeline.updateZoom(isZoomIn: Boolean) {
        if ((!isZoomIn && currZoom - 1 < maxZoomOut) || (isZoomIn && currZoom + 1 > maxZoomIn)) {
            return
        }

        view.tryCloseTilePopup()
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

    private fun MapPipeline.updateMapOffset() {
        xMapOff = (-xViewOff / iconSize + 0.5f).toInt()
        yMapOff = (-yViewOff / iconSize + 0.5f).toInt()
    }

    private fun MapPipeline.updateMousePosition() {
        xMouse = Mouse.getX() * viewZoom - xViewOff
        yMouse = Mouse.getY() * viewZoom - yViewOff

        var xMouseMapNew = xMouse.toInt() / iconSize + 1
        var yMouseMapNew = yMouse.toInt() / iconSize + 1

        selectedMap?.let { map ->
            xMouseMapNew = if (xMouseMapNew < 1 || xMouseMapNew > map.maxX) OUT_OF_BOUNDS else xMouseMapNew
            yMouseMapNew = if (yMouseMapNew < 1 || yMouseMapNew > map.maxY) OUT_OF_BOUNDS else yMouseMapNew

            if (xMouseMapNew != xMouseMap || yMouseMapNew != yMouseMap) {
                xMouseMap = xMouseMapNew
                yMouseMap = yMouseMapNew
                Frame.update()
            }
        }
    }
}
