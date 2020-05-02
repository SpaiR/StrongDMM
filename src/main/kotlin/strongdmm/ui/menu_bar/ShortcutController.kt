package strongdmm.ui.menu_bar

import org.lwjgl.glfw.GLFW
import strongdmm.service.shortcut.Shortcut
import strongdmm.service.shortcut.refactor.ShortcutHandler
import strongdmm.event.EventHandler

class ShortcutController(
    viewController: ViewController
) : EventHandler {
    private val shortcutHandler = ShortcutHandler(this)

    init {
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_N, action = viewController::doNewMap)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_O, action = viewController::doOpenMap)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_O, action = viewController::doOpenAvailableMap)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_W, action = viewController::doCloseMap)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_W, action = viewController::doCloseAllMaps)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_S, action = viewController::doSave)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_S, action = viewController::doSaveAll)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_Q, action = viewController::doExit)

        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_Z, action = viewController::doUndo)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_Z, action = viewController::doRedo)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_X, action = viewController::doCut)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_C, action = viewController::doCopy)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_V, action = viewController::doPaste)
        shortcutHandler.addShortcut(GLFW.GLFW_KEY_DELETE, action = viewController::doDelete)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_D, action = viewController::doDeselectAll)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_F, action = viewController::doFindInstance)

        // "Manual" methods since toggle through the buttons switches ImBool status vars automatically.
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_1, action = viewController::toggleAreaLayerManual)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_2, action = viewController::toggleTurfLayerManual)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_3, action = viewController::toggleObjLayerManual)
        shortcutHandler.addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_4, action = viewController::toggleMobLayerManual)
    }
}
