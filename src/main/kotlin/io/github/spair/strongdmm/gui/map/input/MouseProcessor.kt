package io.github.spair.strongdmm.gui.map.input

import io.github.spair.strongdmm.common.OUT_OF_BOUNDS
import io.github.spair.strongdmm.gui.StatusView
import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.MapPipeline
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.map.openTilePopup
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import org.lwjgl.input.Mouse

// Class to consume and process input from mouse.
// Class handles input only in case, when map canvas is in focus.
// For other cases (common swing flow) event driven developments is used.
object MouseProcessor {

    lateinit var mapPipeline: MapPipeline

    private const val LMB = 0
    private const val RMB = 1
    private const val MMB = 2

    private const val MAX_ZOOM_OUT = 0
    private const val MAX_ZOOM_IN = 10
    private const val ZOOM_FACTOR = 1.5f

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
            selectedMapData?.let { map ->
                xMouse = Mouse.getX() * map.viewZoom - map.xViewOff
                yMouse = Mouse.getY() * map.viewZoom - map.yViewOff

                var xMouseMapNew = xMouse.toInt() / iconSize + 1
                var yMouseMapNew = yMouse.toInt() / iconSize + 1

                xMouseMapNew = if (xMouseMapNew < 1 || xMouseMapNew > map.dmm.getMaxX()) OUT_OF_BOUNDS else xMouseMapNew
                yMouseMapNew = if (yMouseMapNew < 1 || yMouseMapNew > map.dmm.getMaxY()) OUT_OF_BOUNDS else yMouseMapNew

                if (xMouseMapNew == OUT_OF_BOUNDS || yMouseMapNew == OUT_OF_BOUNDS) {
                    xMouseMapNew = OUT_OF_BOUNDS
                    yMouseMapNew = OUT_OF_BOUNDS
                }

                if (xMouseMapNew != xMouseMap || yMouseMapNew != yMouseMap) {
                    xMouseMap = xMouseMapNew
                    yMouseMap = yMouseMapNew

                    StatusView.updateCoords(xMouseMap, yMouseMap)

                    tileChanged = true
                    Frame.update()
                }
            }
        }
    }

    private fun handleTileSelection() {
        if (KeyboardProcessor.isCtrlDown() && KeyboardProcessor.isShiftDown() || (KeyboardProcessor.isAltDown())) {
            return // no tile selection with those modifiers
        }

        if (Mouse.isButtonDown(LMB)) {
            if (mapPipeline.xMouseMap == OUT_OF_BOUNDS || mapPipeline.yMouseMap == OUT_OF_BOUNDS) {
                return
            }

            if (!lmbWasPressed) {
                lmbWasPressed = true
                SelectOperation.onStart(mapPipeline.xMouseMap, mapPipeline.yMouseMap)
            } else {
                if (tileChanged) {
                    tileChanged = false
                    mapPipeline.selectedMapData?.let {
                        SelectOperation.onAdd(mapPipeline.xMouseMap, mapPipeline.yMouseMap)
                    }
                }
            }
        } else {
            if (lmbWasPressed) {
                lmbWasPressed = false
                SelectOperation.onStop()
            }
        }
    }

    private fun handleMouseMapMovement() {
        if (Mouse.isButtonDown(MMB) || (Mouse.isButtonDown(LMB) && KeyboardProcessor.isAltDown())) {
            val x = Mouse.getDX()
            val y = Mouse.getDY()

            mapPipeline.run {
                selectedMapData?.let { map ->
                    val xViewOffNew = map.xViewOff + x * map.viewZoom
                    val yViewOffNew = map.yViewOff + y * map.viewZoom

                    if (xViewOffNew != map.xViewOff || yViewOffNew != map.yViewOff) {
                        map.xViewOff = xViewOffNew
                        map.yViewOff = yViewOffNew

                        updateMapOffset()
                        Frame.update()
                    }
                }
            }
        }
    }

    private fun handleZooming() {
        Mouse.getDWheel().takeIf { it != 0 }?.let { zoomOffset ->
            mapPipeline.run {
                selectedMapData?.let { map ->
                    val isZoomIn = zoomOffset > 0

                    if ((!isZoomIn && map.currZoom - 1 < MAX_ZOOM_OUT) || (isZoomIn && map.currZoom + 1 > MAX_ZOOM_IN)) {
                        return
                    }

                    MapView.tryCloseTilePopup()
                    map.currZoom += if (isZoomIn) 1 else -1

                    if (isZoomIn) {
                        map.viewZoom /= ZOOM_FACTOR
                        map.xViewOff -= Mouse.getX() * map.viewZoom / 2
                        map.yViewOff -= Mouse.getY() * map.viewZoom / 2
                    } else {
                        map.xViewOff += Mouse.getX() * map.viewZoom / 2
                        map.yViewOff += Mouse.getY() * map.viewZoom / 2
                        map.viewZoom *= ZOOM_FACTOR
                    }

                    updateMapOffset()
                    Frame.update()
                }
            }
        }
    }

    private fun handleClicks() {
        while (Mouse.next()) {
            if (Mouse.getEventButtonState()) {
                if (MapView.tryCloseTilePopup() && Mouse.getEventButton() != RMB) {
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
        selectedMapData?.let { selectedMap ->
            selectedMap.xMapOff = (-selectedMap.xViewOff / iconSize + 0.5f).toInt()
            selectedMap.yMapOff = (-selectedMap.yViewOff / iconSize + 0.5f).toInt()

            if (synchronizeMaps) {
                syncOpenedMaps(selectedMap)
            }
        }
    }
}
