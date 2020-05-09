package strongdmm.service

import org.lwjgl.glfw.GLFW
import strongdmm.Processable
import strongdmm.Service
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.window.AppWindow

class ApplicationCloseService : Service, EventHandler, Processable {
    override fun process() {
        if (GLFW.glfwWindowShouldClose(AppWindow.windowPtr)) {
            GLFW.glfwSetWindowShouldClose(AppWindow.windowPtr, false)
            sendEvent(TriggerMapHolderService.CloseAllMaps {
                if (it) {
                    AppWindow.isRunning = false
                }
            })
        }
    }
}
