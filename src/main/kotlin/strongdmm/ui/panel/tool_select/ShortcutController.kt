package strongdmm.ui.panel.tool_select

import org.lwjgl.glfw.GLFW
import strongdmm.service.shortcut.ShortcutHandler

class ShortcutController(
    private val state: State
) : ShortcutHandler() {
    lateinit var viewController: ViewController

    init {
        addShortcut(GLFW.GLFW_KEY_1) { viewController.doSelectTool(state.tools[0]) }
        addShortcut(GLFW.GLFW_KEY_2) { viewController.doSelectTool(state.tools[1]) }
        addShortcut(GLFW.GLFW_KEY_3) { viewController.doSelectTool(state.tools[2]) }
    }
}
