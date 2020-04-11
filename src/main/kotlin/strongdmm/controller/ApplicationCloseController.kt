package strongdmm.controller

import org.lwjgl.glfw.GLFW
import strongdmm.event.EventSender
import strongdmm.event.type.controller.TriggerMapHolderController
import strongdmm.window.AppWindow

class ApplicationCloseController : EventSender {
    fun process() {
        if (GLFW.glfwWindowShouldClose(AppWindow.window)) {
            sendEvent(TriggerMapHolderController.CloseAllMaps {
                if (it) {
                    AppWindow.isRunning = false
                } else {
                    GLFW.glfwSetWindowShouldClose(AppWindow.window, false)
                }
            })
        }
    }
}
