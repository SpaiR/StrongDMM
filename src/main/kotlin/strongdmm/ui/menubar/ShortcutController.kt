package strongdmm.ui.menubar

import org.lwjgl.glfw.GLFW
import strongdmm.service.shortcut.Shortcut
import strongdmm.service.shortcut.ShortcutHandler

class ShortcutController(
    viewController: ViewController
) : ShortcutHandler() {
    init {
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_N, action = viewController::doNewMap)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_O, action = viewController::doOpenMap)
        addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_O, action = viewController::doOpenAvailableMap)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_W, action = viewController::doCloseMap)
        addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_W, action = viewController::doCloseAllMaps)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_S, action = viewController::doSave)
        addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_S, action = viewController::doSaveAll)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_Q, action = viewController::doExit)

        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_Z, action = viewController::doUndo)
        addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_Z, action = viewController::doRedo)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_X, action = viewController::doCut)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_C, action = viewController::doCopy)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_V, action = viewController::doPaste)
        addShortcut(GLFW.GLFW_KEY_DELETE, action = viewController::doDelete)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_D, action = viewController::doDeselectAll)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_F, action = viewController::doFindInstance)

        // "Manual" methods since toggle through the buttons switches ImBoolean status vars automatically.
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_1, action = viewController::toggleAreaLayerManual)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_2, action = viewController::toggleTurfLayerManual)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_3, action = viewController::toggleObjLayerManual)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_4, action = viewController::toggleMobLayerManual)

        addShortcut(GLFW.GLFW_KEY_F5, action = viewController::doResetWindows)
        addShortcut(GLFW.GLFW_KEY_F11, action = viewController::doFullscreen)
    }
}
