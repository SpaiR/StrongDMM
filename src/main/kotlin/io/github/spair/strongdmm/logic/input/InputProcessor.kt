package io.github.spair.strongdmm.logic.input

import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasController
import org.lwjgl.input.Mouse

class InputProcessor(private val ctrl: MapCanvasController) {

    fun fire() {
        if (Mouse.isButtonDown(0)) {
            ctrl.xViewOffset += Mouse.getDX()
            ctrl.yViewOffset += Mouse.getDY()
            ctrl.updateMapOffset()
        }
    }
}