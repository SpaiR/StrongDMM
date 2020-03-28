package strongdmm.controller.shortcut

import imgui.ImGui
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal

class ShortcutController : EventSender, ShortcutHandler() {
    fun process() {
        var shortcutToTrigger: Shortcut? = null

        for (shortcut in globalShortcuts) {
            val (firstKey, secondKey, thirdKey) = shortcut

            if (secondKey == -1 && thirdKey == -1 && ImGui.isKeyPressed(firstKey)) {
                shortcutToTrigger = shortcut
            } else if (ImGui.isKeyDown(firstKey)) {
                if (thirdKey != -1) {
                    if (ImGui.isKeyDown(secondKey) && ImGui.isKeyPressed(thirdKey)) {
                        shortcutToTrigger = shortcut
                        break
                    }
                } else if (secondKey != -1) {
                    if (ImGui.isKeyPressed(secondKey)) {
                        if (shortcutToTrigger == null || shortcutToTrigger.weight < shortcut.weight) {
                            shortcutToTrigger = shortcut
                        }
                    }
                }
            }
        }

        shortcutToTrigger?.let {
            sendEvent(EventGlobal.ShortcutTriggered(it))
        }
    }
}
