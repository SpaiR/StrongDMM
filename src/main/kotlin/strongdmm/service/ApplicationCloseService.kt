package strongdmm.service

import org.lwjgl.glfw.GLFW
import strongdmm.Processable
import strongdmm.Service
import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.window.Window

class ApplicationCloseService : Service, EventHandler, Processable {
    override fun process() {
        if (GLFW.glfwWindowShouldClose(Window.ptr)) {
            GLFW.glfwSetWindowShouldClose(Window.ptr, false)
            sendEvent(TriggerMapHolderService.CloseAllMaps {
                if (it) {
                    Window.isRunning = false
                }
            })
        }
    }
}
