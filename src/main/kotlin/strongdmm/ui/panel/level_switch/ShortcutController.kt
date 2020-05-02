package strongdmm.ui.panel.level_switch

import org.lwjgl.glfw.GLFW
import strongdmm.service.shortcut.Shortcut
import strongdmm.service.shortcut.refactor.ShortcutHandler
import strongdmm.event.EventHandler

class ShortcutController(
    viewController: ViewController
) : EventHandler {
    private val shortcutHandler = ShortcutHandler(this)

    init {
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_LEFT, action = viewController::doDecreaseSelectedZ)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_RIGHT, action = viewController::doIncreaseSelectedZ)
    }
}
