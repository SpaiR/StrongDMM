package strongdmm.controller.shortcut

import imgui.ImGui
import org.lwjgl.glfw.GLFW.*
import strongdmm.event.Event
import strongdmm.event.EventSender

class ShortcutController : EventSender, ShortcutHandler() {
    init {
        // Alt+*
        addShortcut(Shortcut.ALT_PAIR, GLFW_KEY_1)
        addShortcut(Shortcut.ALT_PAIR, GLFW_KEY_2)
        addShortcut(Shortcut.ALT_PAIR, GLFW_KEY_3)
        addShortcut(Shortcut.ALT_PAIR, GLFW_KEY_4)

        // Ctrl+*
        addShortcut(Shortcut.CONTROL_PAIR, GLFW_KEY_Z)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW_KEY_O)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW_KEY_S)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW_KEY_1)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW_KEY_2)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW_KEY_3)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW_KEY_4)

        // Ctrl+Shift+*
        addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW_KEY_Z)
        addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW_KEY_O)
    }

    fun process() {
        var shortcutToTrigger: Shortcut? = null

        for ((shortcut, _) in shortcuts) {
            val (firstKey, secondKey, thirdKey) = shortcut

            if (ImGui.isKeyDown(firstKey)) {
                if (thirdKey != -1) {
                    if (ImGui.isKeyDown(secondKey) && ImGui.isKeyPressed(thirdKey)) {
                        shortcutToTrigger = shortcut
                        break
                    }
                } else if (secondKey != -1) {
                    if (ImGui.isKeyPressed(secondKey)) {
                        shortcutToTrigger = shortcut
                    }
                } else {
                    shortcutToTrigger = shortcut
                }
            }
        }

        shortcutToTrigger?.let {
            sendEvent(Event.Global.TriggerShortcut(it))
        }
    }
}
