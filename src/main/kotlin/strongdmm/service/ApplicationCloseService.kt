package strongdmm.service

import org.lwjgl.glfw.GLFW
import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerMapHolderController
import strongdmm.window.AppWindow

class ApplicationCloseService : EventHandler {
    fun process() {
        if (GLFW.glfwWindowShouldClose(AppWindow.windowPtr)) {
            sendEvent(TriggerMapHolderController.CloseAllMaps {
                if (it) {
                    AppWindow.isRunning = false
                } else {
                    GLFW.glfwSetWindowShouldClose(AppWindow.windowPtr, false)
                }
            })
        }
    }
}
