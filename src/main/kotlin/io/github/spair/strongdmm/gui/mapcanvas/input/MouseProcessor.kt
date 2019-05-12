package io.github.spair.strongdmm.gui.mapcanvas.input

import io.github.spair.strongdmm.gui.mapcanvas.Frame
import io.github.spair.strongdmm.gui.mapcanvas.MapGLRenderer
import io.github.spair.strongdmm.gui.mapcanvas.openTilePopup
import io.github.spair.strongdmm.logic.map.OUT_OF_BOUNDS
import org.lwjgl.input.Mouse

// Class to consume and process input from mouse.
// Class handles input only in case, when map canvas is in focus.
// For other cases (common swing flow) event driven developments is used.
object MouseProcessor {

    lateinit var renderer: MapGLRenderer

    private const val LMB = 0
    private const val RMB = 1
    private const val MMB = 2

    fun fire() {
        renderer.updateMousePosition()

        if (Mouse.isButtonDown(MMB)) {
            renderer.updateViewAndMapOffset()
        }

        Mouse.getDWheel().takeIf { it != 0 }?.let {
            renderer.updateZoom(it > 0)
        }

        while (Mouse.next()) {
            if (Mouse.getEventButtonState()) {
                if (renderer.view.tryCloseTilePopup() && Mouse.getEventButton() != RMB) {
                    continue
                }

                when (Mouse.getEventButton()) {
                    LMB -> {
                        if (KeyboardProcessor.isCtrlDown() && KeyboardProcessor.isShiftDown()) {
                            renderer.selectItem = true
                            Frame.update()
                        }
                    }
                    RMB -> renderer.openTilePopup()
                }
            }
        }
    }

    private fun MapGLRenderer.updateViewAndMapOffset() {
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

    private fun MapGLRenderer.updateZoom(isZoomIn: Boolean) {
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

    private fun MapGLRenderer.updateMapOffset() {
        xMapOff = (-xViewOff / iconSize + 0.5f).toInt()
        yMapOff = (-yViewOff / iconSize + 0.5f).toInt()
    }

    private fun MapGLRenderer.updateMousePosition() {
        xMouse = Mouse.getX() * viewZoom - xViewOff
        yMouse = Mouse.getY() * viewZoom - yViewOff

        var xMouseMapNew = xMouse.toInt() / iconSize + 1
        var yMouseMapNew = yMouse.toInt() / iconSize + 1

        xMouseMapNew = if (xMouseMapNew < 1 || xMouseMapNew > selectedMap!!.maxX) OUT_OF_BOUNDS else xMouseMapNew
        yMouseMapNew = if (yMouseMapNew < 1 || yMouseMapNew > selectedMap!!.maxY) OUT_OF_BOUNDS else yMouseMapNew

        if (xMouseMapNew != xMouseMap || yMouseMapNew != yMouseMap) {
            xMouseMap = xMouseMapNew
            yMouseMap = yMouseMapNew
            Frame.update()
        }
    }
}
