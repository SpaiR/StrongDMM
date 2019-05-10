package io.github.spair.strongdmm.gui.mapcanvas.input

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.gui.menubar.Shortcut
import org.lwjgl.input.Keyboard

// Class to consume and process input from keyboard.
// Class handles input only in case, when map canvas is in focus.
// For other cases (common swing flow) event driven developments is used.
object KeyboardProcessor {

    private val menuBarView by diInstance<MenuBarView>()

    private val ctrlMappings = mapOf(
        Keyboard.KEY_Q to Shortcut.CTRL_Q,
        Keyboard.KEY_S to Shortcut.CTRL_S,
        Keyboard.KEY_O to Shortcut.CTRL_O,
        Keyboard.KEY_Z to Shortcut.CTRL_Z
    )

    private val ctrlShiftMappings = mapOf(
        Keyboard.KEY_O to Shortcut.CTRL_SHIFT_O,
        Keyboard.KEY_Z to Shortcut.CTRL_SHIFT_Z
    )

    fun fire() {
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                // Fire shortcut events
                if (isCtrlDown()) {
                    if (isShiftDown()) {
                        ctrlShiftMappings[Keyboard.getEventKey()]?.let(this::fireShortcut)
                    } else {
                        ctrlMappings[Keyboard.getEventKey()]?.let(this::fireShortcut)
                    }
                }
            }
        }
    }

    private fun fireShortcut(shortcut: Shortcut) = menuBarView.fireShortcutEvent(shortcut)

    fun isCtrlDown() = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
    fun isShiftDown() = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
}
