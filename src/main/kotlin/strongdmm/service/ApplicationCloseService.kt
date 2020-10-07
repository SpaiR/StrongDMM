package strongdmm.service

import org.lwjgl.glfw.GLFW
import strongdmm.application.Processable
import strongdmm.application.Service
import strongdmm.event.EventBus
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.window.Window

class ApplicationCloseService : Service, Processable {
    override fun process() {
        if (GLFW.glfwWindowShouldClose(Window.ptr)) {
            GLFW.glfwSetWindowShouldClose(Window.ptr, false)
            EventBus.post(TriggerMapHolderService.CloseAllMaps {
                if (it) {
                    Window.isRunning = false
                }
            })
        }
    }
}
