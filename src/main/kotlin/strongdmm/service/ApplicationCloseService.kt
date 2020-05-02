package strongdmm.service

import org.lwjgl.glfw.GLFW
import strongdmm.Processable
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.window.AppWindow

class ApplicationCloseService : EventHandler, Processable {
    override fun process() {
        if (GLFW.glfwWindowShouldClose(AppWindow.windowPtr)) {
            sendEvent(TriggerMapHolderService.CloseAllMaps {
                if (it) {
                    AppWindow.isRunning = false
                } else {
                    GLFW.glfwSetWindowShouldClose(AppWindow.windowPtr, false)
                }
            })
        }
    }
}
