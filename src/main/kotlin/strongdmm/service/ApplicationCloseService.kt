package strongdmm.service

import org.lwjgl.glfw.GLFW
import strongdmm.event.EventSender
import strongdmm.event.type.controller.TriggerMapHolderController
import strongdmm.window.AppWindow

class ApplicationCloseService : EventSender {
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
