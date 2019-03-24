package io.github.spair.strongdmm.gui.mapcanvas

import org.lwjgl.input.Mouse

// Class to consume and react on user input actions.
class InputProcessor(private val ctrl: MapCanvasController) {

    fun fire() {
        if (Mouse.isButtonDown(0)) {
            ctrl.updateViewAndMapOffset(Mouse.getDX(), Mouse.getDY())
        }

        Mouse.getDWheel().takeIf { it != 0 }?.let {
            ctrl.updateZoom(it > 0)
        }

        ctrl.updateMouseMapPosition()
    }

    private fun MapCanvasController.updateViewAndMapOffset(x: Int, y: Int) {
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
        val xMouseMapNew = (Mouse.getX() * viewZoom - xViewOff).toInt() / iconSize + 1
        val yMouseMapNew = (Mouse.getY() * viewZoom - yViewOff).toInt() / iconSize + 1

        if (xMouseMapNew != xMouseMap || yMouseMapNew != yMouseMap) {
            xMouseMap = xMouseMapNew
            yMouseMap = yMouseMapNew

            Frame.update()
        }
    }
}
