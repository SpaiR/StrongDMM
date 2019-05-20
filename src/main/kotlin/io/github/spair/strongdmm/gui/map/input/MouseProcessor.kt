package io.github.spair.strongdmm.gui.map.input

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.MapPipeline
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

    private var lmbWasPressed = false
    private var tileChanged = false

    fun fire() {
        updateMousePosition()
        handleTileSelection()
        handleMouseMapMovement()
        handleZooming()
        handleClicks()
    }

    private fun updateMousePosition() {
        mapPipeline.run {
            xMouse = Mouse.getX() * viewZoom - xViewOff
            yMouse = Mouse.getY() * viewZoom - yViewOff

            var xMouseMapNew = xMouse.toInt() / iconSize + 1
            var yMouseMapNew = yMouse.toInt() / iconSize + 1

            selectedMap?.let { map ->
                xMouseMapNew = if (xMouseMapNew < 1 || xMouseMapNew > map.maxX) OUT_OF_BOUNDS else xMouseMapNew
                yMouseMapNew = if (yMouseMapNew < 1 || yMouseMapNew > map.maxY) OUT_OF_BOUNDS else yMouseMapNew

                if (xMouseMapNew == OUT_OF_BOUNDS || yMouseMapNew == OUT_OF_BOUNDS) {
                    xMouseMapNew = OUT_OF_BOUNDS
                    yMouseMapNew = OUT_OF_BOUNDS
                }

                if (xMouseMapNew != xMouseMap || yMouseMapNew != yMouseMap) {
                    xMouseMap = xMouseMapNew
                    yMouseMap = yMouseMapNew

                    tileChanged = true
                    Frame.update()
                }
            }
        }
    }

    private fun handleTileSelection() {
        if (KeyboardProcessor.isCtrlDown() && KeyboardProcessor.isShiftDown()) {
            return  // no tile selection with those modifiers
        }

        if (Mouse.isButtonDown(LMB)) {
            if (!lmbWasPressed) {
                lmbWasPressed = true
                mapPipeline.tileSelect.onStart(mapPipeline.xMouseMap, mapPipeline.yMouseMap)
            } else {
                if (tileChanged) {
                    tileChanged = false
                    mapPipeline.selectedMap?.let {
                        mapPipeline.tileSelect.onAdd(mapPipeline.xMouseMap, mapPipeline.yMouseMap)
                    }
                }
            }
        } else {
            if (lmbWasPressed) {
                lmbWasPressed = false
                mapPipeline.tileSelect.onStop()
            }
        }
    }

    private fun handleMouseMapMovement() {
        if (Mouse.isButtonDown(MMB)) {
            val x = Mouse.getDX()
            val y = Mouse.getDY()

            mapPipeline.run {
                val xViewOffNew = xViewOff + x * viewZoom
                val yViewOffNew = yViewOff + y * viewZoom

                if (xViewOffNew != xViewOff || yViewOffNew != yViewOff) {
                    xViewOff = xViewOffNew
                    yViewOff = yViewOffNew

                    updateMapOffset()
                    Frame.update()
                }
            }
        }
    }

    private fun handleZooming() {
        Mouse.getDWheel().takeIf { it != 0 }?.let { zoomOffset ->
            mapPipeline.run {
                val isZoomIn = zoomOffset > 0

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
        }
    }

    private fun handleClicks() {
        while (Mouse.next()) {
            if (Mouse.getEventButtonState()) {
                if (mapPipeline.view.tryCloseTilePopup() && Mouse.getEventButton() != RMB) {
                    continue
                }

                when (Mouse.getEventButton()) {
                    LMB -> {
                        if (KeyboardProcessor.isCtrlDown() && KeyboardProcessor.isShiftDown()) {
                            mapPipeline.isSelectItem = true
                            Frame.update()
                        }
                    }
                    RMB -> mapPipeline.openTilePopup()
                }
            }
        }
    }

    private fun MapPipeline.updateMapOffset() {
        xMapOff = (-xViewOff / iconSize + 0.5f).toInt()
        yMapOff = (-yViewOff / iconSize + 0.5f).toInt()
    }
}
