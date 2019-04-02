package io.github.spair.strongdmm.gui.mapcanvas.input

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.menubar.*
import org.lwjgl.input.Keyboard.*

object KeyboardProcessor  {

    private val menuBarCtrl by diInstance<MenuBarController>()

    private val ctrlMappings = mapOf(
        KEY_Q to Shortcut.CTRL_Q,
        KEY_S to Shortcut.CTRL_S,
        KEY_O to Shortcut.CTRL_O,
        KEY_Z to Shortcut.CTRL_Z
    )

    private val ctrlShiftMappings = mapOf(
        KEY_O to Shortcut.CTRL_SHIFT_O,
        KEY_Z to Shortcut.CTRL_SHIFT_Z
    )

    fun fire() {
        while (next()) {
            if (getEventKeyState()) {
                if (isCtrlDown()) {
                    if (isShiftDown()) {
                        ctrlShiftMappings[getEventKey()]?.let { menuBarCtrl.fireShortcutEvent(it) }
                    } else {
                        ctrlMappings[getEventKey()]?.let { menuBarCtrl.fireShortcutEvent(it) }
                    }
                }
            }
        }
    }

    private fun isCtrlDown() = isKeyDown(KEY_LCONTROL) || isKeyDown(KEY_RCONTROL)
    private fun isShiftDown() = isKeyDown(KEY_LSHIFT) || isKeyDown(KEY_RSHIFT)
}
