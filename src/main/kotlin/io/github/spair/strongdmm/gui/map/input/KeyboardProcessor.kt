package io.github.spair.strongdmm.gui.map.input

import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.gui.menubar.Shortcut
import org.lwjgl.input.Keyboard

// Class to consume and process input from keyboard.
// Class handles input only in case, when map canvas is in focus.
// For other cases (common swing flow) event driven developments is used.
object KeyboardProcessor {

    private val ctrlMappings = mapOf(
        Keyboard.KEY_Q to Shortcut.CTRL_Q,
        Keyboard.KEY_S to Shortcut.CTRL_S,
        Keyboard.KEY_W to Shortcut.CTRL_W,
        Keyboard.KEY_O to Shortcut.CTRL_O,
        Keyboard.KEY_Z to Shortcut.CTRL_Z,
        Keyboard.KEY_X to Shortcut.CTRL_X,
        Keyboard.KEY_C to Shortcut.CTRL_C,
        Keyboard.KEY_V to Shortcut.CTRL_V,
        Keyboard.KEY_1 to Shortcut.CTRL_1,
        Keyboard.KEY_2 to Shortcut.CTRL_2,
        Keyboard.KEY_3 to Shortcut.CTRL_3,
        Keyboard.KEY_4 to Shortcut.CTRL_4,
        Keyboard.KEY_LEFT to Shortcut.CTRL_LEFT_ARROW,
        Keyboard.KEY_RIGHT to Shortcut.CTRL_RIGHT_ARROW
    )

    private val ctrlShiftMappings = mapOf(
        Keyboard.KEY_W to Shortcut.CTRL_SHIFT_W,
        Keyboard.KEY_S to Shortcut.CTRL_SHIFT_S,
        Keyboard.KEY_O to Shortcut.CTRL_SHIFT_O,
        Keyboard.KEY_Z to Shortcut.CTRL_SHIFT_Z
    )

    private val altMappings = mapOf(
        Keyboard.KEY_1 to Shortcut.ALT_1,
        Keyboard.KEY_2 to Shortcut.ALT_2,
        Keyboard.KEY_3 to Shortcut.ALT_3
    )

    private val plainMappings = mapOf(
        Keyboard.KEY_DELETE to Shortcut.DELETE,
        Keyboard.KEY_ESCAPE to Shortcut.ESCAPE
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
                } else if (isAltDown()) {
                    altMappings[Keyboard.getEventKey()]?.let(this::fireShortcut)
                } else {
                    plainMappings[Keyboard.getEventKey()]?.let(this::fireShortcut)
                }
            }
        }
    }

    private fun fireShortcut(shortcut: Shortcut) = MenuBarView.fireShortcutEvent(shortcut)

    fun isCtrlDown() = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
    fun isShiftDown() = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
    fun isAltDown() = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)
}
