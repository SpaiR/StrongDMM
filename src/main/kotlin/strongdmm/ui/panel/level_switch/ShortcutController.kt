package strongdmm.ui.panel.level_switch

import org.lwjgl.glfw.GLFW
import strongdmm.service.shortcut.Shortcut
import strongdmm.service.shortcut.ShortcutHandler

class ShortcutController(
    viewController: ViewController
) {
    private val shortcutHandler = ShortcutHandler()

    init {
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_LEFT, action = viewController::doDecreaseSelectedZ)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_RIGHT, action = viewController::doIncreaseSelectedZ)
    }
}
