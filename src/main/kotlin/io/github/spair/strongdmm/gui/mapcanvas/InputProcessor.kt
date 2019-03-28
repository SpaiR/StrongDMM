package io.github.spair.strongdmm.gui.mapcanvas

import org.lwjgl.input.Mouse

// Class to consume and react on user input actions.
class InputProcessor(private val ctrl: MapCanvasController) {

    companion object {
        private const val LMB = 0
        private const val RMB = 1
    }

    fun fire() {
        ctrl.updateMouseMapPosition()

        if (Mouse.isButtonDown(LMB)) {
            ctrl.updateViewAndMapOffset()
        }

        Mouse.getDWheel().takeIf { it != 0 }?.let {
            ctrl.updateZoom(it > 0)
        }

        while (Mouse.next()) {
            if (Mouse.getEventButtonState()) {
                if (ctrl.view.tryCloseTilePopup() && Mouse.getEventButton() != RMB) {
                    continue
                }

                if (Mouse.getEventButton() == RMB) {
                    ctrl.openTilePopup()
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
        ctrl.view.tryCloseTilePopup()

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
