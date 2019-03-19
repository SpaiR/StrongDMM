package io.github.spair.strongdmm.gui.mapcanvas

import org.lwjgl.input.Mouse

class InputProcessor(private val ctrl: MapCanvasController) {

    fun fire() {
        if (Mouse.isButtonDown(0)) {
            ctrl.updateViewAndMapOffset(Mouse.getDX(), Mouse.getDY())
        }
    }
}