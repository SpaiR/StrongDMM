package io.github.spair.strongdmm.gui.mapcanvas.input

import org.lwjgl.input.Keyboard

object Keyboard {

    val isCtrlDown get() = isKeyDown(Keyboard.KEY_LCONTROL) || isKeyDown(Keyboard.KEY_RCONTROL)
    val isShiftPressed get() = isKeyDown(Keyboard.KEY_LSHIFT) || isKeyDown(Keyboard.KEY_RSHIFT)
    val isAltPressed get() = isKeyDown(Keyboard.KEY_LMENU) || isKeyDown(Keyboard.KEY_RMENU)

    private var keyState = mutableMapOf<Int, Boolean>()
    private var prevKeyState = mutableMapOf<Int, Boolean>()

    fun update() {
        prevKeyState = keyState.toMutableMap()

        while (Keyboard.next()) {
            if (keyState.containsKey(Keyboard.getEventKey())) {
                if (Keyboard.getEventKeyState())
                    keyState.replace(Keyboard.getEventKey(), true)
                else
                    keyState.replace(Keyboard.getEventKey(), false)
            } else {
                keyState[Keyboard.getEventKey()] = true
            }
        }
    }

    fun isKeyDown(keyCode: Int) = keyState.getOrDefault(keyCode, false)
    fun isKeyUp(keyCode: Int) = !keyState.getOrDefault(keyCode, false)

    fun isKeyPressed(keyCode: Int) = keyState.getOrDefault(keyCode, false) && !prevKeyState.getOrDefault(keyCode, false)
    fun isKeyReleased(keyCode: Int) = !keyState.getOrDefault(keyCode, false) && prevKeyState.getOrDefault(keyCode, false)
}