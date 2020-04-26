package strongdmm.ui.tool_select_panel

import org.lwjgl.glfw.GLFW
import strongdmm.controller.shortcut.refactor.ShortcutHandler
import strongdmm.event.EventHandler

class ShortcutController(
    private val state: State
) : EventHandler {
    private val shortcutHandler = ShortcutHandler(this)

    lateinit var viewController: ViewController

    init {
        shortcutHandler.addShortcut(GLFW.GLFW_KEY_1) { viewController.doSelectTool(state.tools[0]) }
        shortcutHandler.addShortcut(GLFW.GLFW_KEY_2) { viewController.doSelectTool(state.tools[1]) }
        shortcutHandler.addShortcut(GLFW.GLFW_KEY_3) { viewController.doSelectTool(state.tools[2]) }
    }
}
