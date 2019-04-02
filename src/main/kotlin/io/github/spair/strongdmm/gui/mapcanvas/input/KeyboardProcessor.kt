package io.github.spair.strongdmm.gui.mapcanvas.input

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.menubar.*
import org.lwjgl.input.Keyboard.*

object KeyboardProcessor  {

    private val menuBarCtrl by diInstance<MenuBarController>()

    private val ctrlMappings = mapOf(
        KEY_Q to SHORTCUT_CTRL_Q,
        KEY_S to SHORTCUT_CTRL_S,
        KEY_O to SHORTCUT_CTRL_O,
        KEY_Z to SHORTCUT_CTRL_Z
    )

    private val ctrlShiftMappings = mapOf(
        KEY_O to SHORTCUT_CTRL_SHIFT_O,
        KEY_Z to SHORTCUT_CTRL_SHIFT_Z
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
