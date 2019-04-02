package io.github.spair.strongdmm.gui.mapcanvas.input

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.menubar.MenuBarController
import io.github.spair.strongdmm.gui.menubar.SHORTCUT_CTRL_Q
import org.lwjgl.input.Keyboard.*

object KeyboardProcessor  {

    private val menuBarController by diInstance<MenuBarController>()

    fun fire() {
        Keyboard.update()

        if (Keyboard.isCtrlDown) {
            if (Keyboard.isKeyPressed(KEY_Q)) menuBarController.fireShortcutEvent(SHORTCUT_CTRL_Q)
        }
    }
}

