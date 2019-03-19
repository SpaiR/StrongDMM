package io.github.spair.strongdmm.gui.mapcanvas

import org.lwjgl.input.Mouse

class InputProcessor(private val ctrl: MapCanvasController) {

    fun fire() {
        if (Mouse.isButtonDown(0)) {
            ctrl.xViewOff += Mouse.getDX()
            ctrl.yViewOff += Mouse.getDY()
            ctrl.updateMapOffset()
        }
    }
}