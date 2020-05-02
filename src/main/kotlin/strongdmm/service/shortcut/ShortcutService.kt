package strongdmm.service.shortcut

import imgui.ImGui
import strongdmm.Processable
import strongdmm.Service
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction

class ShortcutService : Service, EventHandler, Processable {
    override fun process() {
        var shortcutToTrigger: Shortcut? = null

        for (shortcut in ShortcutHandler.globalShortcuts) {
            val (firstKey, secondKey, thirdKey) = shortcut

            if (secondKey == -1 && thirdKey == -1 && ImGui.isKeyPressed(firstKey)) {
                if (shortcutToTrigger == null || shortcutToTrigger.weight < shortcut.weight) {
                    shortcutToTrigger = shortcut
                }
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
            if (!ImGui.isAnyItemActive()) {
                sendEvent(Reaction.ShortcutTriggered(it))
            }
        }
    }
}
