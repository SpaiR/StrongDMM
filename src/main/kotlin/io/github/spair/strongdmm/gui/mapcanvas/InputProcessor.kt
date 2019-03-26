package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.logic.dme.VAR_NAME
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import org.kodein.di.erased.instance
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import javax.swing.JMenu
import javax.swing.JMenuItem

const val LMB = 0
const val RMB = 1

// Class to consume and react on user input actions.
class InputProcessor(private val ctrl: MapCanvasController) {

    private val dmiProvider by DI.instance<DmiProvider>()

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

    private fun MapCanvasController.openTilePopup() {
        view.createAndShowTilePopup(Mouse.getX(), Display.getHeight() - Mouse.getY()) { popup ->
            selectedMap!!.getTile(xMouseMap, yMouseMap)!!.tileItems.forEach { tileItem ->
                val menu = JMenu("${tileItem.getVar(VAR_NAME)} (${tileItem.type})").apply { popup.add(this) }

                dmiProvider.getDmi(tileItem.icon)?.let { dmi ->
                    dmi.getIconState(tileItem.iconState)?.let { iconState ->
                        menu.icon = iconState.getIconSprite(tileItem.dir).scaledIcon
                    }
                }

                menu.add(JMenuItem("View Variables"))
            }
        }
    }
}
